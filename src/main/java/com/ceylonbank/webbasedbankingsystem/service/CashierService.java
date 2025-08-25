package com.ceylonbank.webbasedbankingsystem.service;

import com.ceylonbank.webbasedbankingsystem.entity.*;
import com.ceylonbank.webbasedbankingsystem.exception.BusinessException;
import com.ceylonbank.webbasedbankingsystem.repository.*;
import com.ceylonbank.webbasedbankingsystem.security.CustomUserDetails;
import com.ceylonbank.webbasedbankingsystem.util.AuditLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CashierService {

    @Autowired private AccountRepository accountRepository;
    @Autowired private AccountTypeRepository accountTypeRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private TransactionService transactionService;

    public record ApprovalRow(Integer accountId, String customerName, String accountNumber, String accountType, BigDecimal initialDeposit, String requestedAt) {}

    public List<ApprovalRow> getPendingApprovals() {
        List<Account> accounts = accountRepository.findAll().stream()
                .filter(a -> "Pending".equalsIgnoreCase(a.getStatus()))
                .collect(Collectors.toList());
        Map<Integer, String> userNames = userRepository.findAll().stream()
                .collect(Collectors.toMap(User::getUserId, u -> u.getFirstName() + " " + u.getLastName()));
        Map<Integer, String> typeNames = accountTypeRepository.findAll().stream()
                .collect(Collectors.toMap(AccountType::getTypeId, AccountType::getTypeName));
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return accounts.stream().map(a -> new ApprovalRow(
                a.getAccountId(),
                userNames.getOrDefault(a.getUserId(), "Unknown"),
                a.getAccountNumber(),
                typeNames.getOrDefault(a.getTypeId(), "N/A"),
                a.getBalance(),
                a.getOpenedAt() != null ? a.getOpenedAt().format(fmt) : ""
        )).collect(Collectors.toList());
    }

    @Transactional
    public void approveAccount(Integer accountId) {
        Account acc = accountRepository.findById(accountId).orElseThrow(() -> new BusinessException("Account not found"));
        acc.setStatus("Approved");
        acc.setIsActive(true);
        acc.setApprovedBy(getCurrentUserId());
        if (acc.getOpenedAt() == null) {
            acc.setOpenedAt(LocalDateTime.now());
        }
        accountRepository.save(acc);
        
        // Log the account approval action using Singleton AuditLogger
        AuditLogger.getInstance().logAction(getCurrentUserId(), "ACCOUNT_APPROVE", 
                "Approved account " + acc.getAccountNumber());
    }

    @Transactional
    public void rejectAccount(Integer accountId) {
        Account acc = accountRepository.findById(accountId).orElseThrow(() -> new BusinessException("Account not found"));
        acc.setStatus("Rejected");
        acc.setIsActive(false);
        accountRepository.save(acc);
        
        // Log the account rejection action using Singleton AuditLogger
        AuditLogger.getInstance().logAction(getCurrentUserId(), "ACCOUNT_REJECT", 
                "Rejected account " + acc.getAccountNumber());
    }

    @Transactional
    public String process(String type, String srcAccNum, String destAccNum, BigDecimal amount, String description) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Amount must be positive");
        }
        Integer cashier = getCurrentUserId();
        String canonType = canonicalizeType(type);
        // Resolve account IDs by account numbers
        Account src = accountRepository.findAll().stream().filter(a -> srcAccNum != null && srcAccNum.equals(a.getAccountNumber())).findFirst()
                .orElseThrow(() -> new BusinessException("Source account not found"));
        
        // Check if source account has pending status
        if ("Pending".equalsIgnoreCase(src.getStatus())) {
            throw new BusinessException("Cannot perform transactions with pending accounts");
        }
        
        // Check if source account has sufficient balance for withdrawal/transfer
        if ("Withdrawal".equals(canonType) || "Transfer".equals(canonType)) {
            if (src.getBalance().compareTo(amount) < 0) {
                throw new BusinessException("Insufficient balance. Current balance: Rs. " + 
                    src.getBalance() + ", Required: Rs. " + amount);
            }
        }
        
        Account dst = null;
        if ("Transfer".equals(canonType)) {
            dst = accountRepository.findAll().stream().filter(a -> destAccNum != null && destAccNum.equals(a.getAccountNumber())).findFirst()
                    .orElseThrow(() -> new BusinessException("Destination account not found"));
            
            // Check if destination account has pending status
            if ("Pending".equalsIgnoreCase(dst.getStatus())) {
                throw new BusinessException("Cannot transfer to a Pending accounts");
            }
            
            // Check if destination account is active
            if (!dst.getIsActive()) {
                throw new BusinessException("Cannot transfer to inactive accounts");
            }
        }
        
        // For deposits, check if source account is active
        if ("Deposit".equals(canonType)) {
            if (!src.getIsActive()) {
                throw new BusinessException("Cannot deposit to inactive accounts");
            }
        }
        String reference = generateTxnReference();
        try {
            transactionService.processTransaction(src.getAccountId(), dst != null ? dst.getAccountId() : null, canonType, amount, description, reference, cashier);
        } catch (RuntimeException ex) {
            throw new BusinessException(ex.getMessage() != null ? ex.getMessage() : "Transaction failed");
        }
        // Create notifications
        if ("Deposit".equals(canonType)) {
            notify(src.getUserId(), "Your account was credited with Rs. " + amount + " by deposit", "Deposit");
        } else if ("Withdrawal".equals(canonType)) {
            notify(src.getUserId(), "Your account was debited with Rs. " + amount + " by withdrawal", "Withdrawal");
        } else if ("Transfer".equals(canonType) && dst != null) {
            notify(src.getUserId(), "Rs. " + amount + " transferred from your account", "Transfer");
            notify(dst.getUserId(), "Rs. " + amount + " received to your account", "Transfer");
        }
        
        // Log the transaction processing action using Singleton AuditLogger
        AuditLogger.getInstance().logAction(getCurrentUserId(), "TRANSACTION", 
                canonType + " processed. Ref=" + reference);
        
        return reference;
    }

    public record DailyRow(Integer transactionId, String accountNumbers, String type, String amount, String referenceNumber, String timestamp, String status) {}

    public List<DailyRow> getTodayByCashier() {
        Integer cashier = getCurrentUserId();
        LocalDate today = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return transactionRepository.findAll().stream()
                .filter(t -> t.getCreatedAt() != null && t.getCreatedAt().toLocalDate().isEqual(today))
                .filter(t -> cashier.equals(t.getPerformedBy()))
                .map(t -> new DailyRow(
                        t.getTransactionId(),
                        formatAccountNumbers(t),
                        t.getType(),
                        t.getAmount().toPlainString(),
                        t.getReferenceNumber(),
                        t.getCreatedAt() != null ? t.getCreatedAt().format(fmt) : "",
                        t.getStatus()
                ))
                .collect(Collectors.toList());
    }

    public record Summary(int transactionCount, String totalAmount) {}

    public Summary getTodaySummary() {
        Integer cashier = getCurrentUserId();
        LocalDate today = LocalDate.now();
        var list = transactionRepository.findAll().stream()
                .filter(t -> t.getCreatedAt() != null && t.getCreatedAt().toLocalDate().isEqual(today))
                .filter(t -> cashier.equals(t.getPerformedBy()))
                .toList();
        BigDecimal total = list.stream().map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Summary(list.size(), total.toPlainString());
    }

    private void notify(Integer userId, String message, String type) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setMessage(message);
        n.setType(type);
        n.setIsRead(false);
        notificationRepository.save(n);
    }

    private Integer getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) auth.getPrincipal()).getUserId();
        }
        return null;
    }

    private String formatAccountNumbers(Transaction t) {
        String src = t.getAccountId() != null ? accountRepository.findById(t.getAccountId()).map(Account::getAccountNumber).orElse("") : "";
        String dst = t.getTargetAccountId() != null ? accountRepository.findById(t.getTargetAccountId()).map(Account::getAccountNumber).orElse("") : "";
        return (dst != null && !dst.isEmpty()) ? (src + " -> " + dst) : src;
    }

    private String generateTxnReference() {
        String base = Long.toString(System.currentTimeMillis(), 36).toUpperCase();
        return "TXN" + base.substring(Math.max(0, base.length() - 9));
    }

    private String canonicalizeType(String input) {
        if (input == null) return "";
        String t = input.trim().toLowerCase();
        return switch (t) {
            case "deposit", "dep", "depo" -> "Deposit";
            case "withdrawal", "withdraw", "wd" -> "Withdrawal";
            case "transfer", "xfer", "xf" -> "Transfer";
            default -> input;
        };
    }
}



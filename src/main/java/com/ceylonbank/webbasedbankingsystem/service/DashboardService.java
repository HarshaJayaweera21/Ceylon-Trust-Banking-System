//package com.ceylonbank.webbasedbankingsystem.service;
//
//import com.ceylonbank.webbasedbankingsystem.entity.Account;
//import com.ceylonbank.webbasedbankingsystem.entity.Loan;
//import com.ceylonbank.webbasedbankingsystem.entity.Transaction;
//import com.ceylonbank.webbasedbankingsystem.repository.AccountRepository;
//import com.ceylonbank.webbasedbankingsystem.repository.LoanRepository;
//import com.ceylonbank.webbasedbankingsystem.repository.TransactionRepository;
//import com.ceylonbank.webbasedbankingsystem.security.CustomUserDetails;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//public class DashboardService {
//
//    @Autowired
//    private AccountRepository accountRepository;
//
//    @Autowired
//    private TransactionRepository transactionRepository;
//
//    @Autowired
//    private LoanRepository loanRepository;
//
//    public List<Account> getAccountsForCurrentUser() {
//        Integer userId = getCurrentUserId();
//        return userId != null ? accountRepository.findByUserId(userId) : List.of();
//    }
//
//    public List<Transaction> getRecentTransactionsForCurrentUser() {
//        Integer userId = getCurrentUserId();
//        return userId != null ? transactionRepository.findRecentByUserId(userId) : List.of();
//    }
//
//    public List<Loan> getLoansForCurrentUser() {
//        Integer userId = getCurrentUserId();
//        return userId != null ? loanRepository.findByUserId(userId) : List.of();
//    }
//
//    private Integer getCurrentUserId() {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
//            return ((CustomUserDetails) auth.getPrincipal()).getUserId();
//        }
//        return null;
//    }
//}

package com.ceylonbank.webbasedbankingsystem.service;

import com.ceylonbank.webbasedbankingsystem.entity.Account;
import com.ceylonbank.webbasedbankingsystem.entity.AccountType;
import com.ceylonbank.webbasedbankingsystem.entity.Loan;
import com.ceylonbank.webbasedbankingsystem.entity.Transaction;
import com.ceylonbank.webbasedbankingsystem.repository.AccountRepository;
import com.ceylonbank.webbasedbankingsystem.repository.AccountTypeRepository;
import com.ceylonbank.webbasedbankingsystem.repository.LoanRepository;
import com.ceylonbank.webbasedbankingsystem.repository.TransactionRepository;
import com.ceylonbank.webbasedbankingsystem.security.CustomUserDetails;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private AccountTypeRepository accountTypeRepository;

    public List<Account> getAccountsForCurrentUser() {
        Integer userId = getCurrentUserId();
        return userId != null ? accountRepository.findByUserId(userId) : List.of();
    }

    public List<Transaction> getRecentTransactionsForCurrentUser() {
        Integer userId = getCurrentUserId();
        return userId != null ? transactionRepository.findRecentByUserId(userId) : List.of();
    }

    public List<TransactionDTO> getEnrichedTransactionDTOs() {
        List<Transaction> transactions = getRecentTransactionsForCurrentUser();
        Map<Integer, String> accountTypeMap = getAccountTypeMapForCurrentUser();
        return transactions.stream().map(tx -> {
            TransactionDTO dto = new TransactionDTO();
            dto.setType(tx.getType());
            dto.setCreatedAt(tx.getCreatedAt().format(DATE_FORMATTER));
            dto.setDescription(tx.getDescription() != null ? tx.getDescription() : "N/A");
            dto.setAmount(tx.getAmount().toPlainString());
            dto.setAccountType(accountTypeMap.getOrDefault(tx.getAccountId(), "Unknown"));
            return dto;
        }).collect(Collectors.toList());
    }

    private Map<Integer, String> getAccountTypeMapForCurrentUser() {
        List<Account> accounts = getAccountsForCurrentUser();
        Map<Integer, Integer> accountToTypeId = accounts.stream()
                .collect(Collectors.toMap(Account::getAccountId, Account::getTypeId));
        List<AccountType> types = accountTypeRepository.findAll();
        Map<Integer, String> typeIdToName = types.stream()
                .collect(Collectors.toMap(AccountType::getTypeId, AccountType::getTypeName));
        Map<Integer, String> accountToTypeName = new HashMap<>();
        accountToTypeId.forEach((accId, typeId) ->
                accountToTypeName.put(accId, typeIdToName.getOrDefault(typeId, "Unknown")));
        return accountToTypeName;
    }

    public List<Loan> getLoansForCurrentUser() {
        Integer userId = getCurrentUserId();
        return userId != null ? loanRepository.findByUserId(userId) : List.of();
    }

    private Integer getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) auth.getPrincipal()).getUserId();
        }
        return null;
    }

    @Data
    public static class TransactionDTO {
        private String type;
        private String createdAt;
        private String description;
        private String amount;
        private String accountType;
    }
}
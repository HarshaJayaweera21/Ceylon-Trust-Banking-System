package com.ceylonbank.webbasedbankingsystem.controller;

import com.ceylonbank.webbasedbankingsystem.entity.Account;
import com.ceylonbank.webbasedbankingsystem.entity.Transaction;
import com.ceylonbank.webbasedbankingsystem.entity.User;
import com.ceylonbank.webbasedbankingsystem.exception.BusinessException;
import com.ceylonbank.webbasedbankingsystem.security.CustomUserDetails;
import com.ceylonbank.webbasedbankingsystem.service.AccountService;
import com.ceylonbank.webbasedbankingsystem.service.CashierService;
import com.ceylonbank.webbasedbankingsystem.service.TransactionService;
import com.ceylonbank.webbasedbankingsystem.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cashier")
public class CashierController {

    private static final Logger logger = LoggerFactory.getLogger(CashierController.class);

    @Autowired
    private CashierService cashierService;

    @Autowired
    private UserService userService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/dashboard")
    public String view(Model model, Authentication authentication) {
        CustomUserDetails details = (CustomUserDetails) authentication.getPrincipal();
        model.addAttribute("cashier", details.getUser());
        model.addAttribute("summary", cashierService.getTodaySummary());
        return "cashier-dashboard";
    }

    @GetMapping("/api/approvals")
    @ResponseBody
    public Object approvals() {
        return cashierService.getPendingApprovals();
    }

    @PostMapping("/api/approvals/{accountId}/approve")
    @ResponseBody
    public ResponseEntity<?> approve(@PathVariable Integer accountId) {
        try {
            cashierService.approveAccount(accountId);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (BusinessException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @PostMapping("/api/approvals/{accountId}/reject")
    @ResponseBody
    public ResponseEntity<?> reject(@PathVariable Integer accountId) {
        try {
            cashierService.rejectAccount(accountId);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (BusinessException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    public record TxnRequest(String type, String sourceAccountNumber, String destinationAccountNumber, BigDecimal amount, String description) {}

    @PostMapping("/api/transactions")
    @ResponseBody
    public ResponseEntity<?> transact(@RequestBody TxnRequest req) {
        try {
            String ref = cashierService.process(req.type, req.sourceAccountNumber, req.destinationAccountNumber, req.amount, req.description);
            return ResponseEntity.ok(Map.of("referenceNumber", ref, "message", "Transaction successful"));
        } catch (BusinessException ex) {
            String msg = ex.getMessage() != null ? ex.getMessage() : "Transaction failed";
            return ResponseEntity.badRequest().body(Map.of("message", msg));
        }
    }

    @GetMapping("/api/history/today")
    @ResponseBody
    public Object today() {
        return cashierService.getTodayByCashier();
    }

    @GetMapping("/api/summary/today")
    @ResponseBody
    public Object summaryToday() {
        return cashierService.getTodaySummary();
    }


    @GetMapping("/api/search/customer")
    @ResponseBody
    public ResponseEntity<?> searchCustomer(@RequestParam String searchTerm) {
        try {
            logger.info("Cashier searching for customers with term: {}", searchTerm);
            
            // Search by NIC, account number, or name
            List<User> customers = userService.searchCustomers(searchTerm);
            
            logger.info("Found {} customers for search term: {}", customers.size(), searchTerm);
            
            if (customers.isEmpty()) {
                return ResponseEntity.ok(Map.of("customers", List.of(), "message", "No customers found"));
            }
            
            // For each customer, get their accounts and recent transactions
            List<Map<String, Object>> customerDetails = customers.stream()
                .map(customer -> {
                    List<Account> accounts = accountService.getAccountsByUserId(customer.getUserId());
                    List<Transaction> recentTransactions = transactionService.getRecentTransactionsByUserId(customer.getUserId(), 5);
                    
                    // Set account type for transactions
                    for (Transaction transaction : recentTransactions) {
                        // Find the account for this transaction
                        Account account = accounts.stream()
                            .filter(acc -> acc.getAccountId().equals(transaction.getAccountId()))
                            .findFirst()
                            .orElse(null);
                        
                        if (account != null) {
                            transaction.setAccountType(account.getTypeName());
                        }
                    }
                    
                    // Create simplified customer data
                    Map<String, Object> customerData = new java.util.HashMap<>();
                    customerData.put("userId", customer.getUserId());
                    customerData.put("firstName", customer.getFirstName());
                    customerData.put("lastName", customer.getLastName());
                    customerData.put("email", customer.getEmail());
                    customerData.put("nic", customer.getNic());
                    customerData.put("street", customer.getStreet());
                    customerData.put("city", customer.getCity());
                    customerData.put("postalCode", customer.getPostalCode());
                    customerData.put("dateOfBirth", customer.getDateOfBirth());
                    
                    // Get customer phone numbers
                    List<Map<String, Object>> phonesData = customer.getPhones().stream()
                        .map(phone -> {
                            Map<String, Object> phoneMap = new java.util.HashMap<>();
                            phoneMap.put("phoneNumber", phone.getPhoneNumber());
                            phoneMap.put("phoneType", phone.getPhoneType());
                            return phoneMap;
                        })
                        .toList();
                    customerData.put("phones", phonesData);
                    
                    // Create simplified accounts data
                    List<Map<String, Object>> accountsData = accounts.stream()
                        .map(account -> {
                            Map<String, Object> accountMap = new java.util.HashMap<>();
                            accountMap.put("accountId", account.getAccountId());
                            accountMap.put("accountNumber", account.getAccountNumber());
                            accountMap.put("typeName", account.getTypeName());
                            accountMap.put("balance", account.getBalance());
                            accountMap.put("status", account.getStatus());
                            accountMap.put("isActive", account.getIsActive());
                            return accountMap;
                        })
                        .toList();
                    
                    // Create simplified transactions data
                    List<Map<String, Object>> transactionsData = recentTransactions.stream()
                        .map(transaction -> {
                            Map<String, Object> transactionMap = new java.util.HashMap<>();
                            transactionMap.put("transactionId", transaction.getTransactionId());
                            transactionMap.put("type", transaction.getType());
                            transactionMap.put("amount", transaction.getAmount());
                            transactionMap.put("description", transaction.getDescription());
                            transactionMap.put("accountType", transaction.getAccountType());
                            transactionMap.put("createdAt", transaction.getCreatedAt());
                            return transactionMap;
                        })
                        .toList();
                    
                    Map<String, Object> result = new java.util.HashMap<>();
                    result.put("customer", customerData);
                    result.put("accounts", accountsData);
                    result.put("recentTransactions", transactionsData);
                    return result;
                })
                .toList();
            
            return ResponseEntity.ok(Map.of("customers", customerDetails));
        } catch (Exception e) {
            logger.error("Error searching customers with term '{}': {}", searchTerm, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to search customers: " + e.getMessage()));
        }
    }

    @PostMapping("/api/account/{accountId}/close")
    @ResponseBody
    public ResponseEntity<?> closeAccount(@PathVariable Integer accountId, Authentication authentication) {
        try {
            logger.info("Cashier {} attempting to close account {}", 
                ((CustomUserDetails) authentication.getPrincipal()).getUser().getUserId(), accountId);
            
            accountService.closeAccount(accountId);
            
            logger.info("Account {} successfully closed", accountId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Account closed successfully"));
        } catch (Exception e) {
            logger.error("Error closing account {}: {}", accountId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping("/api/account/{accountId}/open")
    @ResponseBody
    public ResponseEntity<?> openAccount(@PathVariable Integer accountId, Authentication authentication) {
        try {
            logger.info("Cashier {} attempting to open account {}", 
                ((CustomUserDetails) authentication.getPrincipal()).getUser().getUserId(), accountId);
            
            accountService.openAccount(accountId);
            
            logger.info("Account {} successfully opened", accountId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Account opened successfully"));
        } catch (Exception e) {
            logger.error("Error opening account {}: {}", accountId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}



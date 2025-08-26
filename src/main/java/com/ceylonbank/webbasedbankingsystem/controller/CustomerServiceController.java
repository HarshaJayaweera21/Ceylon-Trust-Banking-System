package com.ceylonbank.webbasedbankingsystem.controller;

import com.ceylonbank.webbasedbankingsystem.entity.Account;
import com.ceylonbank.webbasedbankingsystem.entity.SupportTicket;
import com.ceylonbank.webbasedbankingsystem.entity.Transaction;
import com.ceylonbank.webbasedbankingsystem.entity.User;
import com.ceylonbank.webbasedbankingsystem.repository.SupportTicketRepository;
import com.ceylonbank.webbasedbankingsystem.security.CustomUserDetails;
import com.ceylonbank.webbasedbankingsystem.service.AccountService;
import com.ceylonbank.webbasedbankingsystem.service.SupportTicketService;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/support")
public class CustomerServiceController {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomerServiceController.class);
    
    @Autowired
    private SupportTicketService supportTicketService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AccountService accountService;
    
    @Autowired
    private TransactionService transactionService;
    
    @Autowired
    private SupportTicketRepository supportTicketRepository;
    
    @GetMapping("/dashboard")
    public String showCustomerServiceDashboard(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Unauthenticated access attempt to customer service dashboard");
            return "redirect:/login";
        }
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User cse = userDetails.getUser();
        model.addAttribute("cse", cse);
        
        try {
            // Get dashboard statistics
            Map<String, Object> stats = supportTicketService.getDashboardStats();
            model.addAttribute("stats", stats);
            
            // Get open tickets
            List<SupportTicket> openTickets = supportTicketService.getOpenTickets();
            model.addAttribute("openTickets", openTickets);
            
            // Get resolved tickets today
            List<SupportTicket> resolvedToday = supportTicketService.getTicketsResolvedToday();
            model.addAttribute("resolvedToday", resolvedToday);
            
            // Get all resolved tickets for the solved cases section
            List<SupportTicket> allResolvedTickets = supportTicketService.getResolvedTickets();
            model.addAttribute("allResolvedTickets", allResolvedTickets);
            
        } catch (Exception e) {
            logger.error("Error fetching customer service dashboard data for CSE {}: {}", cse.getUserId(), e.getMessage(), e);
            
            // Provide fallback values to prevent template errors
            Map<String, Object> fallbackStats = Map.of(
                "openTickets", 0L,
                "resolvedToday", 0L,
                "openToday", 0L
            );
            model.addAttribute("stats", fallbackStats);
            model.addAttribute("openTickets", List.of());
            model.addAttribute("resolvedToday", List.of());
            model.addAttribute("allResolvedTickets", List.of());
            model.addAttribute("error", "Error fetching dashboard data: " + e.getMessage());
        }
        
        return "customer-service-dashboard";
    }
    
    @GetMapping("/ticket/{ticketId}")
    @ResponseBody
    public ResponseEntity<?> getTicketDetails(@PathVariable Integer ticketId) {
        try {
            logger.info("Fetching ticket details for ticketId: {}", ticketId);
            
            Optional<SupportTicket> ticketOpt = supportTicketService.getTicketById(ticketId);
            if (ticketOpt.isEmpty()) {
                logger.warn("Ticket not found for ticketId: {}", ticketId);
                return ResponseEntity.badRequest().body(Map.of("error", "Ticket not found"));
            }
            
            SupportTicket ticket = ticketOpt.get();
            User customer = ticket.getUser();
            
            logger.info("Ticket found: {}, Customer: {}", ticket.getTicketId(), customer != null ? customer.getUserId() : "null");
            
            // Ensure customer data is loaded
            if (customer == null) {
                logger.warn("Customer not found for ticket: {}", ticketId);
                return ResponseEntity.badRequest().body(Map.of("error", "Customer not found for this ticket"));
            }
            
            // Get customer's accounts
            List<Account> customerAccounts = accountService.getAccountsByUserId(customer.getUserId());
            logger.info("Found {} accounts for customer: {}", customerAccounts.size(), customer.getUserId());
            
            // Get customer's last 5 transactions
            List<Transaction> recentTransactions = transactionService.getRecentTransactionsByUserId(customer.getUserId(), 5);
            logger.info("Found {} transactions for customer: {}", recentTransactions.size(), customer.getUserId());
            
            // Set account type for transactions
            for (Transaction transaction : recentTransactions) {
                // Find the account for this transaction
                Account account = customerAccounts.stream()
                    .filter(acc -> acc.getAccountId().equals(transaction.getAccountId()))
                    .findFirst()
                    .orElse(null);
                
                if (account != null) {
                    transaction.setAccountType(account.getTypeName());
                }
            }
            
            // Create simplified response to avoid serialization issues
            Map<String, Object> response = new java.util.HashMap<>();
            
            // Simplified ticket data
            Map<String, Object> ticketData = new java.util.HashMap<>();
            ticketData.put("ticketId", ticket.getTicketId());
            ticketData.put("subject", ticket.getSubject());
            ticketData.put("message", ticket.getMessage());
            ticketData.put("status", ticket.getStatus());
            ticketData.put("createdAt", ticket.getCreatedAt());
            ticketData.put("resolvedAt", ticket.getResolvedAt());
            response.put("ticket", ticketData);
            
            // Simplified customer data
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
            
            response.put("customer", customerData);
            
            // Simplified accounts data
            List<Map<String, Object>> accountsData = customerAccounts.stream()
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
            response.put("accounts", accountsData);
            
            // Simplified transactions data
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
            response.put("recentTransactions", transactionsData);
            
            logger.info("Successfully prepared response for ticket: {}", ticketId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching ticket details for ticketId {}: {}", ticketId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to fetch ticket details: " + e.getMessage()));
        }
    }
    
    @PostMapping("/ticket/{ticketId}/resolve")
    @ResponseBody
    public ResponseEntity<?> resolveTicket(@PathVariable Integer ticketId, Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Integer cseId = userDetails.getUserId();
            
            SupportTicket resolvedTicket = supportTicketService.resolveTicket(ticketId, cseId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Ticket resolved successfully",
                "ticketId", resolvedTicket.getTicketId()
            ));
        } catch (Exception e) {
            logger.error("Error resolving ticket {}: {}", ticketId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to resolve ticket"));
        }
    }
    
    @DeleteMapping("/ticket/{ticketId}/delete")
    @ResponseBody
    public ResponseEntity<?> deleteTicket(@PathVariable Integer ticketId, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.warn("Unauthenticated user attempted to delete ticket {}", ticketId);
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }
            
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String userRole = userDetails.getUser().getRole().getRoleName();
            
            logger.info("Delete ticket request from user role: {}", userRole);
            
            // Allow Customer Service Executives to delete tickets
            if (!"CustomerServiceExecutive".equals(userRole)) {
                logger.warn("User with role {} attempted to delete ticket {}", userRole, ticketId);
                return ResponseEntity.status(403).body(Map.of("error", "Only Customer Service Executives can delete tickets"));
            }
            
            boolean deleted = supportTicketService.deleteTicket(ticketId);
            
            if (deleted) {
                logger.info("Successfully deleted ticket {}", ticketId);
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Ticket deleted successfully",
                    "ticketId", ticketId
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Failed to delete ticket"));
            }
        } catch (Exception e) {
            logger.error("Error deleting ticket {}: {}", ticketId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/search/customer")
    @ResponseBody
    public ResponseEntity<?> searchCustomer(@RequestParam String searchTerm) {
        try {
            logger.info("Searching for customers with term: {}", searchTerm);
            
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
    
    
    @PostMapping("/ticket/{ticketId}/assign")
    @ResponseBody
    public ResponseEntity<?> assignTicket(@PathVariable Integer ticketId, Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Integer cseId = userDetails.getUserId();
            
            SupportTicket assignedTicket = supportTicketService.assignTicket(ticketId, cseId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Ticket assigned successfully",
                "ticketId", assignedTicket.getTicketId()
            ));
        } catch (Exception e) {
            logger.error("Error assigning ticket {}: {}", ticketId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to assign ticket"));
        }
    }
    
    @GetMapping("/test/database")
    @ResponseBody
    public ResponseEntity<?> testDatabase() {
        try {
            // Test user count
            long userCount = userService.getAllUsers().size();
            
            // Test customer count
            List<User> customers = userService.getUsersByRole("Customer");
            long customerCount = customers.size();
            
            // Test account count
            long accountCount = 0;
            for (User customer : customers) {
                List<Account> accounts = accountService.getAccountsByUserId(customer.getUserId());
                accountCount += accounts.size();
            }
            
            // Test support tickets count
            long ticketCount = 0;
            try {
                List<SupportTicket> allTickets = supportTicketService.getAllTickets();
                ticketCount = allTickets.size();
            } catch (Exception e) {
                logger.warn("SupportTickets table might not exist: {}", e.getMessage());
            }
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "userCount", userCount,
                "customerCount", customerCount,
                "accountCount", accountCount,
                "ticketCount", ticketCount,
                "message", "Database connection successful"
            ));
        } catch (Exception e) {
            logger.error("Database test failed: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Database test failed: " + e.getMessage()
            ));
        }
    }
    
    @PostMapping("/test/create-sample-tickets")
    @ResponseBody
    public ResponseEntity<?> createSampleTickets() {
        try {
            // Get first customer to create tickets for
            List<User> customers = userService.getUsersByRole("Customer");
            if (customers.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "No customers found to create tickets for"
                ));
            }
            
            User customer = customers.get(0);
            
            // Create sample support tickets
            SupportTicket ticket1 = supportTicketService.createTicket(
                customer, 
                "Account Access Issue", 
                "I cannot access my online banking account. Please help me reset my password."
            );
            
            SupportTicket ticket2 = supportTicketService.createTicket(
                customer, 
                "Transaction Query", 
                "I see a transaction that I don't recognize. Can you please investigate this transaction?"
            );
            
            SupportTicket ticket3 = supportTicketService.createTicket(
                customer, 
                "Card Replacement", 
                "My debit card was lost. I need a replacement card urgently."
            );
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Sample tickets created successfully",
                "ticketsCreated", 3,
                "ticketIds", List.of(ticket1.getTicketId(), ticket2.getTicketId(), ticket3.getTicketId())
            ));
        } catch (Exception e) {
            logger.error("Failed to create sample tickets: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Failed to create sample tickets: " + e.getMessage()
            ));
        }
    }
    
    @GetMapping("/test/debug-tickets")
    @ResponseBody
    public ResponseEntity<?> debugTickets() {
        try {
            // Test individual repository methods
            long totalTickets = supportTicketRepository.count();
            long openTickets = supportTicketRepository.countByStatus("Open");
            long resolvedTickets = supportTicketRepository.countByStatus("Resolved");
            
            // Test date-based queries
            LocalDate today = LocalDate.now();
            long resolvedToday = supportTicketRepository.countResolvedToday(today);
            long openToday = supportTicketRepository.countOpenToday(today);
            
            // Get all tickets
            List<SupportTicket> allTickets = supportTicketRepository.findAll();
            
            // Get open tickets
            List<SupportTicket> openTicketsList = supportTicketRepository.findByStatusOrderByCreatedAtDesc("Open");
            
            // Get resolved tickets
            List<SupportTicket> resolvedTicketsList = supportTicketRepository.findByStatusOrderByResolvedAtDesc("Resolved");
            
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("status", "success");
            response.put("totalTickets", totalTickets);
            response.put("openTickets", openTickets);
            response.put("resolvedTickets", resolvedTickets);
            response.put("resolvedToday", resolvedToday);
            response.put("openToday", openToday);
            response.put("today", today.toString());
            response.put("allTicketsCount", allTickets.size());
            response.put("openTicketsCount", openTicketsList.size());
            response.put("resolvedTicketsCount", resolvedTicketsList.size());
            response.put("sampleTickets", allTickets.stream().limit(5).map(t -> {
                Map<String, Object> ticketMap = new java.util.HashMap<>();
                ticketMap.put("id", t.getTicketId());
                ticketMap.put("subject", t.getSubject());
                ticketMap.put("status", t.getStatus());
                ticketMap.put("createdAt", t.getCreatedAt());
                ticketMap.put("resolvedAt", t.getResolvedAt());
                return ticketMap;
            }).toList());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Debug tickets failed: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Debug failed: " + e.getMessage());
            errorResponse.put("exception", e.getClass().getSimpleName());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @GetMapping("/test/debug-customer-search")
    @ResponseBody
    public ResponseEntity<?> debugCustomerSearch(@RequestParam String searchTerm) {
        try {
            logger.info("Debug customer search for term: {}", searchTerm);
            
            // Test individual search methods
            List<User> allUsers = userService.getAllUsers();
            List<User> customers = userService.getUsersByRole("Customer");
            
            // Test search
            List<User> searchResults = userService.searchCustomers(searchTerm);
            
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("status", "success");
            response.put("searchTerm", searchTerm);
            response.put("totalUsers", allUsers.size());
            response.put("totalCustomers", customers.size());
            response.put("searchResultsCount", searchResults.size());
            response.put("searchResults", searchResults.stream().map(u -> Map.of(
                "userId", u.getUserId(),
                "firstName", u.getFirstName(),
                "lastName", u.getLastName(),
                "email", u.getEmail(),
                "nic", u.getNic(),
                "role", u.getRole().getRoleName()
            )).toList());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Debug customer search failed: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Debug failed: " + e.getMessage());
            errorResponse.put("exception", e.getClass().getSimpleName());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @GetMapping("/test/simple-ticket/{ticketId}")
    @ResponseBody
    public ResponseEntity<?> getSimpleTicketDetails(@PathVariable Integer ticketId) {
        try {
            logger.info("Simple ticket details for ticketId: {}", ticketId);
            
            Optional<SupportTicket> ticketOpt = supportTicketService.getTicketById(ticketId);
            if (ticketOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Ticket not found"));
            }
            
            SupportTicket ticket = ticketOpt.get();
            
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("ticketId", ticket.getTicketId());
            response.put("subject", ticket.getSubject());
            response.put("status", ticket.getStatus());
            response.put("message", "Simple test successful");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Simple ticket test failed: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "Simple test failed: " + e.getMessage()));
        }
    }
    
    @GetMapping("/test/step-by-step-ticket/{ticketId}")
    @ResponseBody
    public ResponseEntity<?> getStepByStepTicketDetails(@PathVariable Integer ticketId) {
        try {
            logger.info("Step-by-step ticket details for ticketId: {}", ticketId);
            
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("ticketId", ticketId);
            
            // Step 1: Get ticket
            Optional<SupportTicket> ticketOpt = supportTicketService.getTicketById(ticketId);
            if (ticketOpt.isEmpty()) {
                response.put("step1", "FAILED - Ticket not found");
                return ResponseEntity.badRequest().body(response);
            }
            response.put("step1", "SUCCESS - Ticket found");
            
            SupportTicket ticket = ticketOpt.get();
            response.put("ticketSubject", ticket.getSubject());
            response.put("ticketStatus", ticket.getStatus());
            
            // Step 2: Get customer
            User customer = ticket.getUser();
            if (customer == null) {
                response.put("step2", "FAILED - Customer is null");
                return ResponseEntity.ok(response);
            }
            response.put("step2", "SUCCESS - Customer found");
            response.put("customerId", customer.getUserId());
            response.put("customerName", customer.getFirstName() + " " + customer.getLastName());
            
            // Step 3: Get accounts
            try {
                List<Account> customerAccounts = accountService.getAccountsByUserId(customer.getUserId());
                response.put("step3", "SUCCESS - Accounts found");
                response.put("accountsCount", customerAccounts.size());
            } catch (Exception e) {
                response.put("step3", "FAILED - " + e.getMessage());
                return ResponseEntity.ok(response);
            }
            
            // Step 4: Get transactions
            try {
                List<Transaction> recentTransactions = transactionService.getRecentTransactionsByUserId(customer.getUserId(), 5);
                response.put("step4", "SUCCESS - Transactions found");
                response.put("transactionsCount", recentTransactions.size());
            } catch (Exception e) {
                response.put("step4", "FAILED - " + e.getMessage());
                return ResponseEntity.ok(response);
            }
            
            response.put("overall", "SUCCESS - All steps completed");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Step-by-step ticket test failed: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("error", "Step-by-step test failed: " + e.getMessage());
            errorResponse.put("exception", e.getClass().getSimpleName());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @GetMapping("/test/debug-ticket-details")
    @ResponseBody
    public ResponseEntity<?> debugTicketDetails(@RequestParam Integer ticketId) {
        try {
            logger.info("Debug ticket details for ticketId: {}", ticketId);
            
            // Test ticket retrieval
            Optional<SupportTicket> ticketOpt = supportTicketService.getTicketById(ticketId);
            if (ticketOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Ticket not found"));
            }
            
            SupportTicket ticket = ticketOpt.get();
            User customer = ticket.getUser();
            
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("status", "success");
            response.put("ticketId", ticketId);
            
            // Build ticket map safely
            Map<String, Object> ticketMap = new java.util.HashMap<>();
            ticketMap.put("id", ticket.getTicketId());
            ticketMap.put("subject", ticket.getSubject());
            ticketMap.put("status", ticket.getStatus());
            ticketMap.put("createdAt", ticket.getCreatedAt());
            ticketMap.put("resolvedAt", ticket.getResolvedAt());
            response.put("ticket", ticketMap);
            
            if (customer != null) {
                // Build customer map safely
                Map<String, Object> customerMap = new java.util.HashMap<>();
                customerMap.put("userId", customer.getUserId());
                customerMap.put("firstName", customer.getFirstName());
                customerMap.put("lastName", customer.getLastName());
                customerMap.put("email", customer.getEmail());
                customerMap.put("nic", customer.getNic());
                response.put("customer", customerMap);
                
                try {
                    // Test accounts
                    List<Account> accounts = accountService.getAccountsByUserId(customer.getUserId());
                    response.put("accountsCount", accounts.size());
                } catch (Exception e) {
                    response.put("accountsError", e.getMessage());
                }
                
                try {
                    // Test transactions
                    List<Transaction> transactions = transactionService.getRecentTransactionsByUserId(customer.getUserId(), 5);
                    response.put("transactionsCount", transactions.size());
                } catch (Exception e) {
                    response.put("transactionsError", e.getMessage());
                }
            } else {
                response.put("customer", "null");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Debug ticket details failed: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Debug failed: " + e.getMessage());
            errorResponse.put("exception", e.getClass().getSimpleName());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}

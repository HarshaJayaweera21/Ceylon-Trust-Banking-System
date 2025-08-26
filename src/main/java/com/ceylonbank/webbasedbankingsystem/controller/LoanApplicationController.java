package com.ceylonbank.webbasedbankingsystem.controller;

import com.ceylonbank.webbasedbankingsystem.entity.Account;
import com.ceylonbank.webbasedbankingsystem.entity.Loan;
import com.ceylonbank.webbasedbankingsystem.entity.User;
import com.ceylonbank.webbasedbankingsystem.security.CustomUserDetails;
import com.ceylonbank.webbasedbankingsystem.service.AccountService;
import com.ceylonbank.webbasedbankingsystem.service.LoanService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/loan-application")
public class LoanApplicationController {

    private static final Logger logger = LoggerFactory.getLogger(LoanApplicationController.class);

    @Autowired
    private LoanService loanService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String showLoanApplicationPage(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Unauthenticated access attempt to loan application page");
            return "redirect:/login";
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User customer = userDetails.getUser();
        model.addAttribute("user", customer);
        
        try {
            // Get customer's accounts
            List<Account> accounts = accountService.getAccountsByUserId(customer.getUserId());
            model.addAttribute("accounts", accounts);
            
            // Check if customer has at least one active account
            boolean hasActiveAccount = accounts.stream().anyMatch(Account::getIsActive);
            model.addAttribute("hasActiveAccount", hasActiveAccount);
            
            // Get customer's loan applications
            List<Loan> myLoans = loanService.getLoansByUserId(customer.getUserId());
            model.addAttribute("myLoans", myLoans);
            
            logger.info("Customer {} has {} loan applications", customer.getUserId(), myLoans.size());
            if (myLoans.isEmpty()) {
                logger.info("No loan applications found for customer {}", customer.getUserId());
            } else {
                for (Loan loan : myLoans) {
                    logger.info("Loan {}: {} - {} LKR", loan.getLoanId(), loan.getLoanType(), loan.getAmount());
                }
            }
            
            // Get available loan types and their interest rates
            Map<String, BigDecimal> availableLoanTypes = loanService.getAvailableLoanTypes();
            model.addAttribute("availableLoanTypes", availableLoanTypes);
            
            logger.info("Loan application page loaded for customer {} with {} accounts and {} loans", 
                       customer.getUserId(), accounts.size(), myLoans.size());
            
        } catch (Exception e) {
            logger.error("Error loading loan application page for customer {}: {}", customer.getUserId(), e.getMessage());
            model.addAttribute("error", "Error loading loan application page. Please try again later.");
            model.addAttribute("accounts", List.of());
            model.addAttribute("hasActiveAccount", false);
            model.addAttribute("myLoans", List.of());
            model.addAttribute("availableLoanTypes", new HashMap<>());
        }

        return "loan-application";
    }


    @PostMapping("/apply")
    @ResponseBody
    public ResponseEntity<?> applyForLoan(@RequestParam String loanType,
                                        @RequestParam BigDecimal amount,
                                        @RequestParam Integer termMonths,
                                        Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User customer = userDetails.getUser();
            
            logger.info("Loan application submitted by customer {}: Type={}, Amount={}, Term={} months", 
                       customer.getUserId(), loanType, amount, termMonths);
            
            // Validate customer has active accounts
            List<Account> accounts = accountService.getAccountsByUserId(customer.getUserId());
            boolean hasActiveAccount = accounts.stream().anyMatch(Account::getIsActive);
            
            if (!hasActiveAccount) {
                logger.warn("Loan application rejected for customer {}: No active accounts", customer.getUserId());
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "You need at least one active account to apply for a loan."
                ));
            }
            
            // Apply for loan
            Loan loan = loanService.applyForLoan(customer.getUserId(), loanType, amount, termMonths);
            
            logger.info("Loan application created successfully with ID {} for customer {}", 
                       loan.getLoanId(), customer.getUserId());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Loan application submitted successfully",
                "loanId", loan.getLoanId()
            ));
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid loan application: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Error processing loan application: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to submit loan application. Please try again."
            ));
        }
    }

    @GetMapping("/details/{loanId}")
    @ResponseBody
    public ResponseEntity<?> getLoanDetails(@PathVariable Integer loanId, Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User customer = userDetails.getUser();
            
            // Verify the loan belongs to the customer
            Loan loan = loanService.getLoanById(loanId)
                    .orElseThrow(() -> new IllegalArgumentException("Loan not found"));
            
            if (!loan.getUserId().equals(customer.getUserId())) {
                logger.warn("Unauthorized access attempt to loan {} by customer {}", loanId, customer.getUserId());
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "You are not authorized to view this loan."
                ));
            }
            
            // Prepare loan details for display
            Map<String, Object> loanDetails = new HashMap<>();
            loanDetails.put("loanId", loan.getLoanId());
            loanDetails.put("loanType", loan.getLoanType());
            loanDetails.put("amount", loan.getAmount());
            loanDetails.put("interestRate", loan.getInterestRate());
            loanDetails.put("termMonths", loan.getTermMonths());
            loanDetails.put("status", loan.getStatus());
            loanDetails.put("appliedAt", loan.getAppliedAt() != null ? loan.getAppliedAt().toString() : "N/A");
            
            // Fetch and format user names instead of IDs
            String approvedByName = "N/A";
            if (loan.getApprovedBy() != null) {
                User approvedByUser = userService.getUserById(loan.getApprovedBy()).orElse(null);
                if (approvedByUser != null) {
                    approvedByName = approvedByUser.getFirstName() + " " + approvedByUser.getLastName();
                }
            }
            loanDetails.put("approvedBy", approvedByName);
            
            String reviewedByName = "N/A";
            if (loan.getReviewedBy() != null) {
                User reviewedByUser = userService.getUserById(loan.getReviewedBy()).orElse(null);
                if (reviewedByUser != null) {
                    reviewedByName = reviewedByUser.getFirstName() + " " + reviewedByUser.getLastName();
                }
            }
            loanDetails.put("reviewedBy", reviewedByName);
            
            loanDetails.put("comments", loan.getComments() != null ? loan.getComments() : "N/A");
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "loan", loanDetails
            ));
            
        } catch (IllegalArgumentException e) {
            logger.error("Error fetching loan details: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Error fetching loan details for loan {}: {}", loanId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to fetch loan details. Please try again."
            ));
        }
    }
}

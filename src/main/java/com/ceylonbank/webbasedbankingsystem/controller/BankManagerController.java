package com.ceylonbank.webbasedbankingsystem.controller;

import com.ceylonbank.webbasedbankingsystem.entity.Loan;
import com.ceylonbank.webbasedbankingsystem.entity.User;
import com.ceylonbank.webbasedbankingsystem.security.CustomUserDetails;
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
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/bank-manager")
public class BankManagerController {

    private static final Logger logger = LoggerFactory.getLogger(BankManagerController.class);

    @Autowired
    private LoanService loanService;

    @Autowired
    private UserService userService;

    @GetMapping("/dashboard")
    public String showBankManagerDashboard(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Unauthenticated access attempt to bank manager dashboard");
            return "redirect:/login";
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User manager = userDetails.getUser();
        model.addAttribute("manager", manager);

        try {
            // Get high-value loans that have been reviewed by loan officers
            List<Loan> reviewedLoans = loanService.getLoansForManagerApproval();
            model.addAttribute("reviewedLoans", reviewedLoans);

            // Calculate summary statistics
            int totalApplications = reviewedLoans.size();
            BigDecimal totalAmount = reviewedLoans.stream()
                    .map(Loan::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            model.addAttribute("totalApplications", totalApplications);
            model.addAttribute("totalAmount", totalAmount);

            // Get user details for display
            List<Integer> userIds = reviewedLoans.stream()
                    .map(Loan::getUserId)
                    .distinct()
                    .toList();
            Map<Integer, User> userMap = userService.getUsersByIds(userIds);
            model.addAttribute("userMap", userMap);

            // Get staff users for role management (excluding System Administrator and Customer)
            List<User> staffUsers = userService.getStaffUsers();
            model.addAttribute("staffUsers", staffUsers);

        } catch (Exception e) {
            logger.error("Error fetching bank manager dashboard data for userId {}: {}", userDetails.getUserId(), e.getMessage());
            model.addAttribute("error", "Error fetching dashboard data. Please try again later.");
        }

        return "bank-manager-dashboard";
    }

    @GetMapping("/loan/{loanId}/details")
    @ResponseBody
    public ResponseEntity<?> getLoanDetails(@PathVariable Integer loanId) {
        try {
            Map<String, Object> loanDetails = loanService.getLoanDetailsForReview(loanId);
            return ResponseEntity.ok(loanDetails);
        } catch (RuntimeException e) {
            logger.error("Error fetching loan details for loanId {}: {}", loanId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Loan not found"));
        }
    }

    @PostMapping("/review/{loanId}")
    @ResponseBody
    public ResponseEntity<?> saveReviewComments(@PathVariable Integer loanId, 
                                               @RequestParam String comments,
                                               Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Integer managerId = userDetails.getUserId();
            
            // For bank manager, we're just saving comments, not changing status
            // The loan should already be in "Reviewed" status from loan officer
            loanService.reviewLoan(loanId, managerId, comments);
            
            return ResponseEntity.ok(Map.of("success", true));
        } catch (RuntimeException e) {
            logger.error("Error saving review comments for loanId {}: {}", loanId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to save comments"));
        }
    }

    @PostMapping("/approve/{loanId}")
    @ResponseBody
    public ResponseEntity<?> approveLoan(@PathVariable Integer loanId, Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Integer managerId = userDetails.getUserId();
            
            loanService.approveLoanByManager(loanId, managerId);
            
            return ResponseEntity.ok(Map.of("success", true));
        } catch (RuntimeException e) {
            logger.error("Error approving loan for loanId {}: {}", loanId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to approve loan"));
        }
    }

    @PostMapping("/reject/{loanId}")
    @ResponseBody
    public ResponseEntity<?> rejectLoan(@PathVariable Integer loanId, Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Integer managerId = userDetails.getUserId();
            
            loanService.rejectLoanByManager(loanId, managerId);
            
            return ResponseEntity.ok(Map.of("success", true));
        } catch (RuntimeException e) {
            logger.error("Error rejecting loan for loanId {}: {}", loanId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to reject loan"));
        }
    }

    @PostMapping("/change-role/{userId}")
    @ResponseBody
    public ResponseEntity<?> changeUserRole(@PathVariable Integer userId, 
                                          @RequestParam String newRole,
                                          Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Integer managerId = userDetails.getUserId();
            
            logger.info("Bank Manager {} attempting to change user {} role to {}", managerId, userId, newRole);
            
            // Validate that the new role is one of the allowed staff roles
            List<String> allowedRoles = List.of("Cashier", "LoanOfficer", "CustomerServiceExecutive");
            if (!allowedRoles.contains(newRole)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid role specified"));
            }
            
            // Change the user's role
            userService.changeUserRole(userId, newRole);
            
            logger.info("Successfully changed user {} role to {} by manager {}", userId, newRole, managerId);
            return ResponseEntity.ok(Map.of("success", true, "message", "User role changed successfully"));
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid role change request for user {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error changing role for user {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to change user role"));
        }
    }
}

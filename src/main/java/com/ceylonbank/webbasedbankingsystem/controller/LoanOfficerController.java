package com.ceylonbank.webbasedbankingsystem.controller;

import com.ceylonbank.webbasedbankingsystem.entity.Loan;
import com.ceylonbank.webbasedbankingsystem.entity.LoanDocument;
import com.ceylonbank.webbasedbankingsystem.entity.User;
import com.ceylonbank.webbasedbankingsystem.security.CustomUserDetails;
import com.ceylonbank.webbasedbankingsystem.service.LoanDocumentService;
import com.ceylonbank.webbasedbankingsystem.service.LoanService;
import com.ceylonbank.webbasedbankingsystem.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class LoanOfficerController {

    private static final Logger logger = LoggerFactory.getLogger(LoanOfficerController.class);

    @Autowired
    private LoanService loanService;

    @Autowired
    private UserService userService;

    @Autowired
    private LoanDocumentService loanDocumentService;

    @GetMapping("/loan-officer/dashboard")
    public String showLoanOfficerDashboard(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Unauthenticated access attempt to loan officer dashboard");
            return "redirect:/login";
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User officer = userDetails.getUser();
        model.addAttribute("officer", officer);

        try {
            // Get all loans with status = 'Pending' (both <=500,000 and >500,000)
            List<Loan> pendingLoans = loanService.getLoansForOfficerApproval();
            logger.info("Found {} pending loans", pendingLoans != null ? pendingLoans.size() : 0);
            model.addAttribute("pendingLoans", pendingLoans != null ? pendingLoans : List.of());
            
            // Get approved and rejected loans
            List<Loan> approvedLoans = loanService.getLoansByStatus("Approved");
            List<Loan> rejectedLoans = loanService.getLoansByStatus("Rejected");
            logger.info("Found {} approved loans, {} rejected loans", 
                approvedLoans != null ? approvedLoans.size() : 0, 
                rejectedLoans != null ? rejectedLoans.size() : 0);
            model.addAttribute("approvedLoans", approvedLoans != null ? approvedLoans : List.of());
            model.addAttribute("rejectedLoans", rejectedLoans != null ? rejectedLoans : List.of());
            
            // Get user details for each loan applicant
            Map<Integer, User> userMap = Map.of();
            List<Loan> allLoans = new ArrayList<>();
            if (pendingLoans != null) allLoans.addAll(pendingLoans);
            if (approvedLoans != null) allLoans.addAll(approvedLoans);
            if (rejectedLoans != null) allLoans.addAll(rejectedLoans);
            
            logger.info("Total loans to process: {}", allLoans.size());
            
            if (!allLoans.isEmpty()) {
                List<Integer> userIds = allLoans.stream()
                    .map(Loan::getUserId)
                    .filter(userId -> userId != null)
                    .distinct()
                    .collect(Collectors.toList());
                logger.info("Found {} unique user IDs: {}", userIds.size(), userIds);
                
                if (!userIds.isEmpty()) {
                    userMap = userService.getUsersByIds(userIds);
                    logger.info("Retrieved {} users from database", userMap.size());
                    logger.info("User map keys: {}", userMap.keySet());
                }
            }
            model.addAttribute("userMap", userMap);
            
            // Calculate summary statistics
            int totalApplications = pendingLoans != null ? pendingLoans.size() : 0;
            BigDecimal totalAmount = BigDecimal.ZERO;
            if (pendingLoans != null) {
                totalAmount = pendingLoans.stream()
                    .map(Loan::getAmount)
                    .filter(amount -> amount != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            }
            
            logger.info("Summary: {} applications, total amount: {}", totalApplications, totalAmount);
            model.addAttribute("totalApplications", totalApplications);
            model.addAttribute("totalAmount", totalAmount);
            
        } catch (Exception e) {
            logger.error("Error fetching loan officer dashboard data: {}", e.getMessage());
            model.addAttribute("error", "Error loading dashboard data. Please try again later.");
        }

        return "loan-officer-dashboard";
    }

    @PostMapping("/loan-officer/approve/{loanId}")
    public String approveLoan(@PathVariable Integer loanId, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        try {
            loanService.approveLoan(loanId, userDetails.getUserId());
            return "redirect:/loan-officer/dashboard?success=Loan approved successfully!";
        } catch (Exception e) {
            logger.error("Error approving loan {}: {}", loanId, e.getMessage());
            return "redirect:/loan-officer/dashboard?error=Failed to approve loan. Please try again.";
        }
    }

    @PostMapping("/loan-officer/reject/{loanId}")
    public String rejectLoan(@PathVariable Integer loanId, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        try {
            loanService.rejectLoan(loanId, userDetails.getUserId());
            return "redirect:/loan-officer/dashboard?success=Loan rejected successfully!";
        } catch (Exception e) {
            logger.error("Error rejecting loan {}: {}", loanId, e.getMessage());
            return "redirect:/loan-officer/dashboard?error=Failed to reject loan. Please try again.";
        }
    }

    @PostMapping("/loan-officer/review/{loanId}")
    public String reviewLoan(@PathVariable Integer loanId, 
                           @RequestParam(required = false) String comments,
                           Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        try {
            loanService.reviewLoan(loanId, userDetails.getUserId(), comments);
            return "redirect:/loan-officer/dashboard?success=Loan reviewed successfully!";
        } catch (Exception e) {
            logger.error("Error reviewing loan {}: {}", loanId, e.getMessage());
            return "redirect:/loan-officer/dashboard?error=Failed to review loan. Please try again.";
        }
    }

    @GetMapping("/loan-officer/loan/{loanId}/details")
    @ResponseBody
    public Map<String, Object> getLoanDetails(@PathVariable Integer loanId) {
        try {
            logger.info("Fetching loan details for loan ID: {}", loanId);
            Map<String, Object> details = loanService.getLoanDetailsForReview(loanId);
            logger.info("Successfully retrieved loan details for loan {} with {} documents", 
                       loanId, ((List<?>) details.get("documents")).size());
            return details;
        } catch (Exception e) {
            logger.error("Error fetching loan details for loan {}: {}", loanId, e.getMessage(), e);
            return Map.of("error", "Failed to fetch loan details: " + e.getMessage());
        }
    }

    @PostMapping("/loan-officer/delete/{loanId}")
    public String deleteLoan(@PathVariable Integer loanId, Authentication authentication) {
        try {
            logger.info("Attempting to delete loan with ID: {}", loanId);
            loanService.deleteLoan(loanId);
            logger.info("Loan {} deleted successfully, redirecting to dashboard", loanId);
            return "redirect:/loan-officer/dashboard?success=Loan record deleted successfully!";
        } catch (Exception e) {
            logger.error("Error deleting loan {}: {}", loanId, e.getMessage(), e);
            return "redirect:/loan-officer/dashboard?error=Failed to delete loan record. Please try again.";
        }
    }

    @GetMapping("/loan-officer/download-document/{documentId}")
    @ResponseBody
    public ResponseEntity<?> downloadDocument(@PathVariable Integer documentId, Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Integer officerId = userDetails.getUserId();
            
            logger.info("Loan Officer {} requesting document download for document ID: {}", officerId, documentId);
            
            // Get document and verify it exists
            Optional<LoanDocument> documentOpt = loanDocumentService.getDocumentById(documentId);
            if (documentOpt.isEmpty()) {
                logger.warn("Document {} not found for download request by officer {}", documentId, officerId);
                return ResponseEntity.notFound().build();
            }
            
            LoanDocument document = documentOpt.get();
            
            // Create resource from file path
            Path filePath = Paths.get(document.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());
            
            if (!resource.exists()) {
                logger.warn("File not found for document {}: {}", documentId, filePath.toString());
                return ResponseEntity.notFound().build();
            }

            String contentType = document.getFileType();
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            String originalFileName = loanDocumentService.getOriginalFileNameFromPath(document);
            
            logger.info("Document {} successfully downloaded by loan officer {}", documentId, officerId);
            
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + originalFileName + "\"")
                .body(resource);
                
        } catch (MalformedURLException e) {
            logger.error("Error creating URL resource for document {}: {}", documentId, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error downloading document {} by loan officer: {}", documentId, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/loan-officer/test-data")
    @ResponseBody
    public Map<String, Object> testData() {
        try {
            List<Loan> allLoans = loanService.getAllLoans();
            List<User> allUsers = userService.getAllUsers();
            
            return Map.of(
                "totalLoans", allLoans.size(),
                "totalUsers", allUsers.size(),
                "loans", allLoans.stream().map(loan -> Map.of(
                    "id", loan.getLoanId(),
                    "userId", loan.getUserId(),
                    "type", loan.getLoanType(),
                    "amount", loan.getAmount(),
                    "status", loan.getStatus()
                )).collect(Collectors.toList()),
                "users", allUsers.stream().map(user -> Map.of(
                    "id", user.getUserId(),
                    "firstName", user.getFirstName(),
                    "lastName", user.getLastName(),
                    "username", user.getUsername()
                )).collect(Collectors.toList())
            );
        } catch (Exception e) {
            logger.error("Error in test data endpoint: {}", e.getMessage(), e);
            return Map.of("error", e.getMessage());
        }
    }

    @PostMapping("/loan-officer/create-test-data")
    @ResponseBody
    public Map<String, Object> createTestData() {
        try {
            // Create a test user if none exists
            List<User> users = userService.getAllUsers();
            User testUser;
            
            if (users.isEmpty()) {
                // Create a test user
                testUser = new User();
                testUser.setUsername("testuser");
                testUser.setPasswordHash("$2a$10$test");
                testUser.setFirstName("Test");
                testUser.setLastName("User");
                testUser.setEmail("test@example.com");
                testUser.setNic("123456789V");
                testUser.setIsActive(true);
                // Note: You'll need to set a role ID here
                testUser = userService.createUser(testUser);
            } else {
                testUser = users.get(0);
            }
            
            // Create test loans
            Loan pendingLoan = loanService.applyForLoan(testUser.getUserId(), "Personal Loan", new BigDecimal("100000"), 24);
            Loan approvedLoan = loanService.applyForLoan(testUser.getUserId(), "Home Loan", new BigDecimal("500000"), 60);
            Loan rejectedLoan = loanService.applyForLoan(testUser.getUserId(), "Business Loan", new BigDecimal("200000"), 36);
            
            // Update statuses
            loanService.approveLoan(approvedLoan.getLoanId(), 1);
            loanService.rejectLoan(rejectedLoan.getLoanId(), 1);
            
            return Map.of(
                "success", true,
                "message", "Test data created successfully",
                "pendingLoanId", pendingLoan.getLoanId(),
                "approvedLoanId", approvedLoan.getLoanId(),
                "rejectedLoanId", rejectedLoan.getLoanId()
            );
        } catch (Exception e) {
            logger.error("Error creating test data: {}", e.getMessage(), e);
            return Map.of("error", e.getMessage());
        }
    }

    @GetMapping("/loan-officer/verify-delete/{loanId}")
    @ResponseBody
    public Map<String, Object> verifyDelete(@PathVariable Integer loanId) {
        try {
            List<Loan> allLoans = loanService.getAllLoans();
            Optional<Loan> loanOpt = allLoans.stream()
                .filter(loan -> loan.getLoanId().equals(loanId))
                .findFirst();
            
            return Map.of(
                "loanId", loanId,
                "exists", loanOpt.isPresent(),
                "loanDetails", loanOpt.map(loan -> Map.of(
                    "id", loan.getLoanId(),
                    "status", loan.getStatus(),
                    "type", loan.getLoanType(),
                    "amount", loan.getAmount()
                )).orElse(null),
                "totalLoans", allLoans.size()
            );
        } catch (Exception e) {
            logger.error("Error verifying delete for loan {}: {}", loanId, e.getMessage(), e);
            return Map.of("error", e.getMessage());
        }
    }
}

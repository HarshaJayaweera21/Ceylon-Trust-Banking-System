package com.ceylonbank.webbasedbankingsystem.controller;

import com.ceylonbank.webbasedbankingsystem.entity.Account;
import com.ceylonbank.webbasedbankingsystem.entity.AccountType;
import com.ceylonbank.webbasedbankingsystem.entity.User;
import com.ceylonbank.webbasedbankingsystem.repository.AccountRepository;
import com.ceylonbank.webbasedbankingsystem.security.CustomUserDetails;
import com.ceylonbank.webbasedbankingsystem.service.AccountService;
import com.ceylonbank.webbasedbankingsystem.service.DashboardService;
import com.ceylonbank.webbasedbankingsystem.service.CashierService;
import com.ceylonbank.webbasedbankingsystem.service.LoanService;
import com.ceylonbank.webbasedbankingsystem.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.security.web.csrf.CsrfToken;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private LoanService loanService;

    @Autowired
    private CashierService cashierService;

    @Autowired
    private AccountRepository accountRepository;

    @GetMapping("/dashboard")
    public String showDashboard(Model model, Authentication authentication, CsrfToken csrfToken) {
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Unauthenticated access attempt to /dashboard");
            return "redirect:/login";
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails)) {
            logger.error("Principal is not an instance of CustomUserDetails: {}", principal);
            return "redirect:/access-denied";
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();
        model.addAttribute("user", user);
        
        // Add CSRF token to model
        if (csrfToken != null) {
            model.addAttribute("_csrf", csrfToken);
        }

        try {
            List<Account> accounts = dashboardService.getAccountsForCurrentUser();
            model.addAttribute("accounts", accounts);
            model.addAttribute("transactions", dashboardService.getEnrichedTransactionDTOs());
            model.addAttribute("loans", dashboardService.getLoansForCurrentUser());
            model.addAttribute("notificationsCount", 0); // Placeholder

            List<AccountType> allTypes = accountService.getAllAccountTypes();
            Set<Integer> existingTypeIds = new HashSet<>();
            accounts.forEach(acc -> existingTypeIds.add(acc.getTypeId()));
            List<AccountType> availableTypes = allTypes.stream()
                    .filter(type -> !existingTypeIds.contains(type.getTypeId()))
                    .collect(Collectors.toList());
            model.addAttribute("availableAccountTypes", availableTypes);
            model.addAttribute("showAddAccount", !availableTypes.isEmpty());
            model.addAttribute("availableLoanTypes", loanService.getAvailableLoanTypes());
        } catch (Exception e) {
            logger.error("Error fetching dashboard data for userId {}: {}", userDetails.getUserId(), e.getMessage());
            model.addAttribute("error", "Error fetching dashboard data. Please try again later.");
        }

        return "dashboard";
    }

    @PostMapping("/dashboard/open-account")
    public String openAccount(@RequestParam("typeId") Integer typeId,
                              @RequestParam("nic") String nic,
                              @RequestParam("initialDeposit") BigDecimal initialDeposit,
                              Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        try {
            accountService.createAccount(userDetails.getUserId(), typeId, initialDeposit, nic);
            return "redirect:/dashboard?success=true";
        } catch (IllegalArgumentException e) {
            return "redirect:/dashboard?error=" + e.getMessage();
        }
    }

    @PostMapping("/dashboard/apply-loan")
    public String applyForLoan(@RequestParam("loanType") String loanType,
                              @RequestParam("amount") BigDecimal amount,
                              @RequestParam("termMonths") Integer termMonths,
                              Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        try {
            if (!loanService.validateLoanAmount(amount)) {
                return "redirect:/dashboard?error=Invalid loan amount. Please enter between 10,000 and 10,000,000 LKR.";
            }
            
            loanService.applyForLoan(userDetails.getUserId(), loanType, amount, termMonths);
            return "redirect:/dashboard?success=Loan application submitted successfully!";
        } catch (Exception e) {
            logger.error("Error applying for loan: {}", e.getMessage());
            return "redirect:/dashboard?error=Failed to submit loan application. Please try again.";
        }
    }

    @GetMapping("/dashboard/calculate-emi")
    @ResponseBody
    public String calculateEMI(@RequestParam("amount") BigDecimal amount,
                              @RequestParam("loanType") String loanType,
                              @RequestParam("termMonths") Integer termMonths) {
        try {
            BigDecimal interestRate = loanService.getInterestRateForLoanType(loanType);
            BigDecimal emi = loanService.calculateEMI(amount, interestRate, termMonths);
            return emi.toPlainString();
        } catch (Exception e) {
            logger.error("Error calculating EMI: {}", e.getMessage());
            return "0.00";
        }
    }

    @PostMapping("/dashboard/transfer")
    public String transferMoney(@RequestParam("sourceAccount") String sourceAccount,
                               @RequestParam("targetAccount") String targetAccount,
                               @RequestParam("amount") BigDecimal amount,
                               @RequestParam(value = "description", required = false) String description,
                               Authentication authentication) {
        try {
            // Validate that the source account belongs to the current user
            List<Account> userAccounts = dashboardService.getAccountsForCurrentUser();
            Account sourceAccountObj = userAccounts.stream()
                    .filter(account -> account.getAccountNumber().equals(sourceAccount) && account.getIsActive())
                    .findFirst()
                    .orElse(null);
            
            if (sourceAccountObj == null) {
                return "redirect:/dashboard?error=You can only transfer from your own active accounts.";
            }
            
            // Check if source account has pending status
            if ("Pending".equalsIgnoreCase(sourceAccountObj.getStatus())) {
                return "redirect:/dashboard?error=Cannot perform transactions with pending accounts.";
            }
            
            // Check if source account has sufficient balance for transfer
            if (sourceAccountObj.getBalance().compareTo(amount) < 0) {
                return "redirect:/dashboard?error=Insufficient balance. Current balance: Rs. " + 
                       sourceAccountObj.getBalance() + ", Required: Rs. " + amount;
            }
            
            // Validate that source and target accounts are different
            if (sourceAccount.equals(targetAccount)) {
                return "redirect:/dashboard?error=Cannot transfer to the same account.";
            }
            
            // Check if target account has pending status
            Account targetAccountObj = accountRepository.findAll().stream()
                    .filter(account -> targetAccount.equals(account.getAccountNumber()))
                    .findFirst()
                    .orElse(null);
            
            if (targetAccountObj == null) {
                return "redirect:/dashboard?error=Destination account not found.";
            }
            
            if ("Pending".equalsIgnoreCase(targetAccountObj.getStatus())) {
                return "redirect:/dashboard?error=Cannot transfer to Pending accounts";
            }
            
            // Check if target account is active
            if (!targetAccountObj.getIsActive()) {
                return "redirect:/dashboard?error=Cannot transfer to inactive accounts";
            }
            
            // Use CashierService to process the transfer
            String referenceNumber = cashierService.process("Transfer", sourceAccount, targetAccount, amount, description);
            
            return "redirect:/dashboard?success=Transfer completed successfully! Reference: " + referenceNumber;
        } catch (BusinessException e) {
            logger.error("Transfer failed: {}", e.getMessage());
            return "redirect:/dashboard?error=" + e.getMessage();
        } catch (Exception e) {
            logger.error("Error processing transfer: {}", e.getMessage());
            return "redirect:/dashboard?error=Transfer failed. Please try again.";
        }
    }
}
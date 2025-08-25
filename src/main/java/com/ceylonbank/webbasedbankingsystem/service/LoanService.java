package com.ceylonbank.webbasedbankingsystem.service;

import com.ceylonbank.webbasedbankingsystem.entity.Loan;
import com.ceylonbank.webbasedbankingsystem.entity.LoanDocument;
import com.ceylonbank.webbasedbankingsystem.repository.LoanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LoanService {

    private static final Logger logger = LoggerFactory.getLogger(LoanService.class);

    @Autowired
    private LoanRepository loanRepository;
    
    @Autowired
    private LoanDocumentService loanDocumentService;

    // Loan types with their default interest rates
    private static final Map<String, BigDecimal> LOAN_TYPES = Map.of(
        "Personal Loan", new BigDecimal("12.5"),
        "Home Loan", new BigDecimal("9.0"),
        "Business Loan", new BigDecimal("11.0"),
        "Education Loan", new BigDecimal("7.5"),
        "Vehicle Loan", new BigDecimal("10.0")
    );

    public List<Loan> getLoansByUserId(Integer userId) {
        return loanRepository.findByUserId(userId);
    }

    public Optional<Loan> getLoanById(Integer loanId) {
        return loanRepository.findById(loanId);
    }

    public Loan applyForLoan(Integer userId, String loanType, BigDecimal amount, Integer termMonths) {
        Loan loan = new Loan();
        loan.setUserId(userId);
        loan.setLoanType(loanType);
        loan.setAmount(amount);
        loan.setInterestRate(getInterestRateForLoanType(loanType));
        loan.setTermMonths(termMonths);
        loan.setStatus("Pending");
        
        return loanRepository.save(loan);
    }

    public BigDecimal getInterestRateForLoanType(String loanType) {
        return LOAN_TYPES.getOrDefault(loanType, new BigDecimal("12.5"));
    }

    public Map<String, BigDecimal> getAvailableLoanTypes() {
        return LOAN_TYPES;
    }

    public BigDecimal calculateEMI(BigDecimal principal, BigDecimal annualInterestRate, Integer months) {
        if (principal == null || annualInterestRate == null || months == null || months <= 0) {
            return BigDecimal.ZERO;
        }

        // Convert annual interest rate to monthly rate
        BigDecimal monthlyRate = annualInterestRate.divide(new BigDecimal("1200"), 10, RoundingMode.HALF_UP);
        
        // EMI formula: EMI = [P × R × (1+R)^N] / [(1+R)^N – 1]
        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal onePlusRToN = onePlusR.pow(months);
        
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(onePlusRToN);
        BigDecimal denominator = onePlusRToN.subtract(BigDecimal.ONE);
        
        if (denominator.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    public boolean validateLoanAmount(BigDecimal amount) {
        // Basic validation - minimum 10,000 LKR, maximum 10,000,000 LKR
        return amount != null && 
               amount.compareTo(new BigDecimal("10000")) >= 0 && 
               amount.compareTo(new BigDecimal("10000000")) <= 0;
    }

    // Loan Officer specific methods
    public List<Loan> getLoansForOfficerApproval() {
        // Get all loans with status = 'Pending' (both <=500,000 and >500,000)
        return loanRepository.findAll().stream()
                .filter(loan -> loan != null && "Pending".equalsIgnoreCase(loan.getStatus()))
                .collect(Collectors.toList());
    }

    public List<Loan> getLoansForManagerApproval() {
        // Get loans with amount > 500,000 and status = 'Reviewed'
        return loanRepository.findAll().stream()
                .filter(loan -> loan != null && "Reviewed".equalsIgnoreCase(loan.getStatus()))
                .filter(loan -> loan.getAmount() != null && loan.getAmount().compareTo(new BigDecimal("500000")) > 0)
                .collect(Collectors.toList());
    }

    public void approveLoan(Integer loanId, Integer officerId) {
        Optional<Loan> loanOpt = loanRepository.findById(loanId);
        if (loanOpt.isPresent()) {
            Loan loan = loanOpt.get();
            loan.setStatus("Approved");
            loan.setApprovedBy(officerId);
            loanRepository.save(loan);
        } else {
            throw new RuntimeException("Loan not found");
        }
    }

    public void rejectLoan(Integer loanId, Integer officerId) {
        Optional<Loan> loanOpt = loanRepository.findById(loanId);
        if (loanOpt.isPresent()) {
            Loan loan = loanOpt.get();
            loan.setStatus("Rejected");
            loan.setReviewedBy(officerId);
            loanRepository.save(loan);
        } else {
            throw new RuntimeException("Loan not found");
        }
    }

    public void reviewLoan(Integer loanId, Integer officerId, String comments) {
        Optional<Loan> loanOpt = loanRepository.findById(loanId);
        if (loanOpt.isPresent()) {
            Loan loan = loanOpt.get();
            loan.setStatus("Reviewed");
            loan.setReviewedBy(officerId);
            if (comments != null && !comments.trim().isEmpty()) {
                loan.setComments(comments);
            }
            loanRepository.save(loan);
        } else {
            throw new RuntimeException("Loan not found");
        }
    }

    public void approveLoanByManager(Integer loanId, Integer managerId) {
        Optional<Loan> loanOpt = loanRepository.findById(loanId);
        if (loanOpt.isPresent()) {
            Loan loan = loanOpt.get();
            loan.setStatus("Approved");
            loan.setApprovedBy(managerId);
            loanRepository.save(loan);
        } else {
            throw new RuntimeException("Loan not found");
        }
    }

    public void rejectLoanByManager(Integer loanId, Integer managerId) {
        Optional<Loan> loanOpt = loanRepository.findById(loanId);
        if (loanOpt.isPresent()) {
            Loan loan = loanOpt.get();
            loan.setStatus("Rejected");
            loan.setReviewedBy(managerId);
            loanRepository.save(loan);
        } else {
            throw new RuntimeException("Loan not found");
        }
    }

    public Map<String, Object> getLoanDetailsForReview(Integer loanId) {
        Optional<Loan> loanOpt = loanRepository.findById(loanId);
        if (loanOpt.isPresent()) {
            Loan loan = loanOpt.get();
            
            // Get loan documents
            List<LoanDocument> documents = loanDocumentService.getDocumentsByLoanId(loanId);
            logger.info("Found {} documents for loan ID: {}", documents.size(), loanId);
            List<Map<String, Object>> documentList = documents.stream()
                .map(doc -> {
                    Map<String, Object> docMap = new HashMap<>();
                    docMap.put("documentId", doc.getDocumentId());
                    docMap.put("originalFileName", loanDocumentService.getOriginalFileNameFromPath(doc));
                    docMap.put("fileType", doc.getFileType());
                    docMap.put("fileSize", loanDocumentService.getFormattedFileSize(loanDocumentService.getFileSizeFromFilesystem(doc)));
                    docMap.put("uploadedAt", doc.getUploadedAt().toString());
                    docMap.put("fileIcon", loanDocumentService.getFileTypeIcon(doc.getFileType()));
                    return docMap;
                })
                .toList();
            
            Map<String, Object> details = new HashMap<>();
            details.put("loanId", loan.getLoanId());
            details.put("loanType", loan.getLoanType());
            details.put("amount", loan.getAmount().toPlainString());
            details.put("interestRate", loan.getInterestRate().toPlainString());
            details.put("termMonths", loan.getTermMonths());
            details.put("status", loan.getStatus());
            details.put("appliedAt", loan.getAppliedAt() != null ? 
                loan.getAppliedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "");
            details.put("comments", loan.getComments() != null ? loan.getComments() : "");
            details.put("emi", calculateEMI(loan.getAmount(), loan.getInterestRate(), loan.getTermMonths()).toPlainString());
            details.put("documents", documentList);
            
            return details;
        } else {
            throw new RuntimeException("Loan not found");
        }
    }

    public List<Loan> getLoansByStatus(String status) {
        return loanRepository.findAll().stream()
                .filter(loan -> loan != null && status.equalsIgnoreCase(loan.getStatus()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteLoan(Integer loanId) {
        Optional<Loan> loanOpt = loanRepository.findById(loanId);
        if (loanOpt.isPresent()) {
            Loan loan = loanOpt.get();
            logger.info("Deleting loan: ID={}, Status={}, Type={}, Amount={}", 
                loan.getLoanId(), loan.getStatus(), loan.getLoanType(), loan.getAmount());
            
            // Try both methods to ensure deletion
            loanRepository.delete(loan);
            logger.info("Loan {} deleted successfully using delete(entity)", loanId);
            
            // Verify deletion
            Optional<Loan> verifyOpt = loanRepository.findById(loanId);
            if (verifyOpt.isEmpty()) {
                logger.info("Verification: Loan {} successfully removed from database", loanId);
            } else {
                logger.error("Verification failed: Loan {} still exists in database", loanId);
            }
        } else {
            logger.error("Loan not found with ID: {}", loanId);
            throw new RuntimeException("Loan not found");
        }
    }

    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }
}

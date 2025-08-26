package com.ceylonbank.webbasedbankingsystem.controller;

import com.ceylonbank.webbasedbankingsystem.entity.Loan;
import com.ceylonbank.webbasedbankingsystem.entity.LoanDocument;
import com.ceylonbank.webbasedbankingsystem.entity.User;
import com.ceylonbank.webbasedbankingsystem.security.CustomUserDetails;
import com.ceylonbank.webbasedbankingsystem.service.LoanDocumentService;
import com.ceylonbank.webbasedbankingsystem.service.LoanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/loan-application")
public class LoanDocumentController {

    private static final Logger logger = LoggerFactory.getLogger(LoanDocumentController.class);

    @Autowired
    private LoanDocumentService loanDocumentService;

    @Autowired
    private LoanService loanService;

    /**
     * Upload a document for a loan application
     */
    @PostMapping("/upload-document/{loanId}")
    @ResponseBody
    public ResponseEntity<?> uploadDocument(@PathVariable Integer loanId,
                                          @RequestParam("file") MultipartFile file,
                                          Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User customer = userDetails.getUser();
            
            logger.info("Document upload attempt for loan {} by customer {}", loanId, customer.getUserId());
            
            // Verify the loan belongs to the customer
            Optional<Loan> loanOpt = loanService.getLoanById(loanId);
            if (loanOpt.isEmpty()) {
                logger.warn("Loan not found: {}", loanId);
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Loan not found"
                ));
            }
            
            Loan loan = loanOpt.get();
            if (!loan.getUserId().equals(customer.getUserId())) {
                logger.warn("Unauthorized document upload attempt for loan {} by customer {}", loanId, customer.getUserId());
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "You are not authorized to upload documents for this loan"
                ));
            }
            
            // Upload the document
            LoanDocument document = loanDocumentService.uploadDocument(loanId, file);
            
            logger.info("Document uploaded successfully: {} for loan {}", document.getDocumentId(), loanId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Document uploaded successfully",
                "documentId", document.getDocumentId(),
                "fileName", document.getOriginalFileName(),
                "fileSize", loanDocumentService.getFormattedFileSize(document.getFileSize()),
                "uploadedAt", document.getUploadedAt().toString()
            ));
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid document upload: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        } catch (IOException e) {
            logger.error("File upload error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to upload file. Please try again."
            ));
        } catch (Exception e) {
            logger.error("Unexpected error during document upload: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "An unexpected error occurred. Please try again."
            ));
        }
    }

    /**
     * Get all documents for a loan
     */
    @GetMapping("/documents/{loanId}")
    @ResponseBody
    public ResponseEntity<?> getDocumentsForLoan(@PathVariable Integer loanId, Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User customer = userDetails.getUser();
            
            // Verify the loan belongs to the customer
            Optional<Loan> loanOpt = loanService.getLoanById(loanId);
            if (loanOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Loan not found"
                ));
            }
            
            Loan loan = loanOpt.get();
            if (!loan.getUserId().equals(customer.getUserId())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "You are not authorized to view documents for this loan"
                ));
            }
            
            // Get documents
            List<LoanDocument> documents = loanDocumentService.getDocumentsByLoanId(loanId);
            
            // Format documents for response
            List<Map<String, Object>> documentList = documents.stream()
                .map(doc -> {
                    Map<String, Object> docMap = new HashMap<>();
                    docMap.put("documentId", doc.getDocumentId());
                    docMap.put("originalFileName", loanDocumentService.getOriginalFileNameFromPath(doc));
                    docMap.put("fileType", doc.getFileType());
                    
                    // Get file size from filesystem
                    Long fileSize = loanDocumentService.getFileSizeFromFilesystem(doc);
                    docMap.put("fileSize", loanDocumentService.getFormattedFileSize(fileSize));
                    
                    docMap.put("uploadedAt", doc.getUploadedAt().toString());
                    docMap.put("fileIcon", loanDocumentService.getFileTypeIcon(doc.getFileType()));
                    return docMap;
                })
                .toList();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "documents", documentList
            ));
            
        } catch (Exception e) {
            logger.error("Error fetching documents for loan {}: {}", loanId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to fetch documents. Please try again."
            ));
        }
    }

    /**
     * Delete a document
     */
    @DeleteMapping("/delete-document/{documentId}")
    @ResponseBody
    public ResponseEntity<?> deleteDocument(@PathVariable Integer documentId, Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User customer = userDetails.getUser();
            
            // Get document and verify ownership
            Optional<LoanDocument> documentOpt = loanDocumentService.getDocumentById(documentId);
            if (documentOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Document not found"
                ));
            }
            
            LoanDocument document = documentOpt.get();
            Optional<Loan> loanOpt = loanService.getLoanById(document.getLoanId());
            if (loanOpt.isEmpty() || !loanOpt.get().getUserId().equals(customer.getUserId())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "You are not authorized to delete this document"
                ));
            }
            
            // Delete document
            boolean deleted = loanDocumentService.deleteDocument(documentId);
            if (deleted) {
                logger.info("Document {} deleted by customer {}", documentId, customer.getUserId());
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Document deleted successfully"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Failed to delete document"
                ));
            }
            
        } catch (Exception e) {
            logger.error("Error deleting document {}: {}", documentId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to delete document. Please try again."
            ));
        }
    }

    /**
     * Download a document
     */
    @GetMapping("/download-document/{documentId}")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Integer documentId, Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User customer = userDetails.getUser();
            
            // Get document and verify ownership
            Optional<LoanDocument> documentOpt = loanDocumentService.getDocumentById(documentId);
            if (documentOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            LoanDocument document = documentOpt.get();
            Optional<Loan> loanOpt = loanService.getLoanById(document.getLoanId());
            if (loanOpt.isEmpty() || !loanOpt.get().getUserId().equals(customer.getUserId())) {
                return ResponseEntity.notFound().build();
            }
            
            // Check if file exists
            if (!loanDocumentService.fileExists(document)) {
                return ResponseEntity.notFound().build();
            }
            
            // Create resource
            Path filePath = loanDocumentService.getFilePath(document);
            Resource resource = new UrlResource(filePath.toUri());
            
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            // Determine content type
            String contentType = document.getFileType();
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            String originalFileName = loanDocumentService.getOriginalFileNameFromPath(document);
            
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + originalFileName + "\"")
                .body(resource);
                
        } catch (MalformedURLException e) {
            logger.error("Error creating URL resource for document {}: {}", documentId, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error downloading document {}: {}", documentId, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }
}

package com.ceylonbank.webbasedbankingsystem.service;

import com.ceylonbank.webbasedbankingsystem.entity.Loan;
import com.ceylonbank.webbasedbankingsystem.entity.LoanDocument;
import com.ceylonbank.webbasedbankingsystem.repository.LoanDocumentRepository;
import com.ceylonbank.webbasedbankingsystem.repository.LoanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class LoanDocumentService {

    private static final Logger logger = LoggerFactory.getLogger(LoanDocumentService.class);

    @Autowired
    private LoanDocumentRepository loanDocumentRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Value("${app.upload.dir:uploads/loans}")
    private String uploadDirectory;

    // Allowed file types
    private static final List<String> ALLOWED_FILE_TYPES = List.of(
        "application/pdf",
        "image/jpeg",
        "image/jpg",
        "image/png",
        "image/gif"
    );

    // Maximum file size (10MB)
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * Upload a document for a specific loan
     */
    public LoanDocument uploadDocument(Integer loanId, MultipartFile file) throws IOException {
        // Validate loan exists
        Optional<Loan> loanOpt = loanRepository.findById(loanId);
        if (loanOpt.isEmpty()) {
            throw new IllegalArgumentException("Loan not found with ID: " + loanId);
        }

        // Validate file
        validateFile(file);

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            logger.info("Created upload directory: {}", uploadPath.toAbsolutePath());
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = generateUniqueFilename(fileExtension);
        Path filePath = uploadPath.resolve(uniqueFilename);

        // Save file to filesystem
        try {
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("File saved successfully: {}", filePath.toAbsolutePath());
        } catch (IOException e) {
            logger.error("Failed to save file: {}", e.getMessage(), e);
            throw new IOException("Failed to save file: " + e.getMessage());
        }

        // Save document metadata to database
        LoanDocument document = new LoanDocument();
        document.setLoanId(loanId);
        document.setFilePath(filePath.toString());
        document.setFileType(file.getContentType());
        // Set transient fields for immediate use
        document.setOriginalFileName(originalFilename);
        document.setFileSize(file.getSize());

        LoanDocument savedDocument = loanDocumentRepository.save(document);
        // Re-set transient fields after saving
        savedDocument.setOriginalFileName(originalFilename);
        savedDocument.setFileSize(file.getSize());
        
        logger.info("Document metadata saved to database with ID: {}", savedDocument.getDocumentId());

        return savedDocument;
    }

    /**
     * Get all documents for a specific loan
     */
    public List<LoanDocument> getDocumentsByLoanId(Integer loanId) {
        return loanDocumentRepository.findByLoanIdOrderByUploadedAtDesc(loanId);
    }

    /**
     * Get a specific document by ID
     */
    public Optional<LoanDocument> getDocumentById(Integer documentId) {
        return loanDocumentRepository.findById(documentId);
    }

    /**
     * Delete a document (both file and database record)
     */
    public boolean deleteDocument(Integer documentId) {
        Optional<LoanDocument> documentOpt = loanDocumentRepository.findById(documentId);
        if (documentOpt.isEmpty()) {
            return false;
        }

        LoanDocument document = documentOpt.get();
        
        try {
            // Delete file from filesystem
            Path filePath = Paths.get(document.getFilePath());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                logger.info("File deleted: {}", filePath.toAbsolutePath());
            }

            // Delete database record
            loanDocumentRepository.delete(document);
            logger.info("Document record deleted with ID: {}", documentId);
            return true;
        } catch (IOException e) {
            logger.error("Failed to delete file: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Validate uploaded file
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of 10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_FILE_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("File type not allowed. Only PDF and image files are accepted.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("File name is invalid");
        }
    }

    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * Generate unique filename
     */
    private String generateUniqueFilename(String extension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return timestamp + "_" + uuid + extension;
    }

    /**
     * Get file path for serving files
     */
    public Path getFilePath(LoanDocument document) {
        return Paths.get(document.getFilePath());
    }

    /**
     * Check if file exists on filesystem
     */
    public boolean fileExists(LoanDocument document) {
        return Files.exists(Paths.get(document.getFilePath()));
    }

    /**
     * Get file size from filesystem
     */
    public Long getFileSizeFromFilesystem(LoanDocument document) {
        try {
            Path filePath = Paths.get(document.getFilePath());
            if (Files.exists(filePath)) {
                return Files.size(filePath);
            }
        } catch (IOException e) {
            logger.warn("Could not get file size for document {}: {}", document.getDocumentId(), e.getMessage());
        }
        return null;
    }
    
    /**
     * Get original filename from file path (extract from stored path)
     */
    public String getOriginalFileNameFromPath(LoanDocument document) {
        // Since we can't store original filename, we'll use the stored path
        // In a real implementation, you might want to store this in the database
        Path filePath = Paths.get(document.getFilePath());
        return filePath.getFileName().toString();
    }
    
    /**
     * Get file size in human readable format
     */
    public String getFormattedFileSize(Long bytes) {
        if (bytes == null) return "Unknown";
        
        String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;
        double size = bytes.doubleValue();
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.1f %s", size, units[unitIndex]);
    }

    /**
     * Get file type icon class for display
     */
    public String getFileTypeIcon(String contentType) {
        if (contentType == null) return "fa-file";
        
        return switch (contentType.toLowerCase()) {
            case "application/pdf" -> "fa-file-pdf";
            case "image/jpeg", "image/jpg", "image/png", "image/gif" -> "fa-file-image";
            default -> "fa-file";
        };
    }
}

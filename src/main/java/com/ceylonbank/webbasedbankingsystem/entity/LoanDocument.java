package com.ceylonbank.webbasedbankingsystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "LoanDocuments")
@Data
public class LoanDocument {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DocumentID")
    private Integer documentId;
    
    @Column(name = "LoanID", nullable = false)
    private Integer loanId;
    
    @Column(name = "FilePath", nullable = false, length = 255)
    private String filePath;
    
    @Column(name = "FileType", nullable = false, length = 50)
    private String fileType;
    
    @CreationTimestamp
    @Column(name = "UploadedAt", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;
    
    // Transient fields for handling file information (not stored in database)
    @Transient
    private String originalFileName;
    
    @Transient
    private Long fileSize;
    
    // Constructors
    public LoanDocument() {}
    
    public LoanDocument(Integer loanId, String filePath, String fileType) {
        this.loanId = loanId;
        this.filePath = filePath;
        this.fileType = fileType;
    }
    
    public LoanDocument(Integer loanId, String filePath, String fileType, String originalFileName, Long fileSize) {
        this.loanId = loanId;
        this.filePath = filePath;
        this.fileType = fileType;
        this.originalFileName = originalFileName;
        this.fileSize = fileSize;
    }
}

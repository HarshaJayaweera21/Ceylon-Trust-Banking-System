package com.ceylonbank.webbasedbankingsystem.repository;

import com.ceylonbank.webbasedbankingsystem.entity.LoanDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanDocumentRepository extends JpaRepository<LoanDocument, Integer> {
    
    /**
     * Find all documents for a specific loan
     */
    List<LoanDocument> findByLoanIdOrderByUploadedAtDesc(Integer loanId);
    
    /**
     * Find documents by loan ID and file type
     */
    List<LoanDocument> findByLoanIdAndFileTypeOrderByUploadedAtDesc(Integer loanId, String fileType);
    
    /**
     * Count documents for a specific loan
     */
    long countByLoanId(Integer loanId);
    
    /**
     * Find documents by loan ID with pagination support
     */
    @Query("SELECT ld FROM LoanDocument ld WHERE ld.loanId = :loanId ORDER BY ld.uploadedAt DESC")
    List<LoanDocument> findDocumentsByLoanId(@Param("loanId") Integer loanId);
    
    /**
     * Check if a document exists for a specific loan and file path
     */
    boolean existsByLoanIdAndFilePath(Integer loanId, String filePath);
}

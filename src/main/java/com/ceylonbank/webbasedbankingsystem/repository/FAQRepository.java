package com.ceylonbank.webbasedbankingsystem.repository;

import com.ceylonbank.webbasedbankingsystem.entity.FAQ;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FAQRepository extends JpaRepository<FAQ, Integer> {
    
    // Find FAQs by category (case insensitive)
    List<FAQ> findByCategoryIgnoreCaseOrderByCreatedAtDesc(String category);
    
    // Find all FAQs ordered by creation date
    List<FAQ> findAllByOrderByCreatedAtDesc();
    
    // Search FAQs by question or answer
    @Query("SELECT f FROM FAQ f WHERE LOWER(f.question) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(f.answer) LIKE LOWER(CONCAT('%', :searchTerm, '%')) ORDER BY f.createdAt DESC")
    List<FAQ> searchFAQs(@Param("searchTerm") String searchTerm);
    
    // Find distinct categories
    @Query("SELECT DISTINCT f.category FROM FAQ f WHERE f.category IS NOT NULL ORDER BY f.category")
    List<String> findDistinctCategories();
    
    // Find FAQs created by a specific user
    List<FAQ> findByCreatedByUserIdOrderByCreatedAtDesc(Integer userId);
    
    // Find recent FAQs (last 30 days)
    @Query(value = "SELECT * FROM FAQs WHERE CreatedAt >= DATEADD(day, -30, GETDATE()) ORDER BY CreatedAt DESC", nativeQuery = true)
    List<FAQ> findRecentFAQs();
    
    // Count FAQs by category
    @Query("SELECT COUNT(f) FROM FAQ f WHERE f.category = :category")
    long countByCategory(@Param("category") String category);
    
    // Find FAQs with non-null categories
    @Query("SELECT f FROM FAQ f WHERE f.category IS NOT NULL AND f.category != '' ORDER BY f.category, f.createdAt DESC")
    List<FAQ> findFAQsWithCategories();
}

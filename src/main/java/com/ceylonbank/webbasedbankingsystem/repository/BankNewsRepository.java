package com.ceylonbank.webbasedbankingsystem.repository;

import com.ceylonbank.webbasedbankingsystem.entity.BankNews;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankNewsRepository extends JpaRepository<BankNews, Integer> {
    
    // Find all non-expired news ordered by posted date
    @Query(value = "SELECT * FROM BankNews WHERE (ExpiryDate IS NULL OR ExpiryDate > GETDATE()) ORDER BY PostedAt DESC", nativeQuery = true)
    List<BankNews> findAllNonExpiredOrderByPostedAtDesc();
    
    // Find all non-expired public news ordered by posted date
    @Query(value = "SELECT * FROM BankNews WHERE IsPublic = 1 AND (ExpiryDate IS NULL OR ExpiryDate > GETDATE()) ORDER BY PostedAt DESC", nativeQuery = true)
    List<BankNews> findAllNonExpiredPublicOrderByPostedAtDesc();
    
    // Find all non-expired staff news (not public) ordered by posted date
    @Query(value = "SELECT * FROM BankNews WHERE IsPublic = 0 AND (ExpiryDate IS NULL OR ExpiryDate > GETDATE()) ORDER BY PostedAt DESC", nativeQuery = true)
    List<BankNews> findAllNonExpiredStaffOrderByPostedAtDesc();
    
    // Find news by category (non-expired)
    @Query(value = "SELECT * FROM BankNews WHERE Category = ?1 AND (ExpiryDate IS NULL OR ExpiryDate > GETDATE()) ORDER BY PostedAt DESC", nativeQuery = true)
    List<BankNews> findByCategoryAndNonExpiredOrderByPostedAtDesc(String category);
    
    // Find public news by category (non-expired)
    @Query(value = "SELECT * FROM BankNews WHERE Category = ?1 AND IsPublic = 1 AND (ExpiryDate IS NULL OR ExpiryDate > GETDATE()) ORDER BY PostedAt DESC", nativeQuery = true)
    List<BankNews> findPublicByCategoryAndNonExpiredOrderByPostedAtDesc(String category);
    
    // Find staff news by category (non-expired)
    @Query(value = "SELECT * FROM BankNews WHERE Category = ?1 AND IsPublic = 0 AND (ExpiryDate IS NULL OR ExpiryDate > GETDATE()) ORDER BY PostedAt DESC", nativeQuery = true)
    List<BankNews> findStaffByCategoryAndNonExpiredOrderByPostedAtDesc(String category);
    
    // Search news by title or content (non-expired)
    @Query(value = "SELECT * FROM BankNews WHERE (Title LIKE %?1% OR Content LIKE %?1%) AND (ExpiryDate IS NULL OR ExpiryDate > GETDATE()) ORDER BY PostedAt DESC", nativeQuery = true)
    List<BankNews> searchNonExpiredNews(String searchTerm);
    
    // Search public news by title or content (non-expired)
    @Query(value = "SELECT * FROM BankNews WHERE (Title LIKE %?1% OR Content LIKE %?1%) AND IsPublic = 1 AND (ExpiryDate IS NULL OR ExpiryDate > GETDATE()) ORDER BY PostedAt DESC", nativeQuery = true)
    List<BankNews> searchNonExpiredPublicNews(String searchTerm);
    
    // Search staff news by title or content (non-expired)
    @Query(value = "SELECT * FROM BankNews WHERE (Title LIKE %?1% OR Content LIKE %?1%) AND IsPublic = 0 AND (ExpiryDate IS NULL OR ExpiryDate > GETDATE()) ORDER BY PostedAt DESC", nativeQuery = true)
    List<BankNews> searchNonExpiredStaffNews(String searchTerm);
    
    // Find distinct categories (non-expired)
    @Query(value = "SELECT DISTINCT Category FROM BankNews WHERE (ExpiryDate IS NULL OR ExpiryDate > GETDATE()) ORDER BY Category", nativeQuery = true)
    List<String> findDistinctCategoriesNonExpired();
    
    // Find distinct public categories (non-expired)
    @Query(value = "SELECT DISTINCT Category FROM BankNews WHERE IsPublic = 1 AND (ExpiryDate IS NULL OR ExpiryDate > GETDATE()) ORDER BY Category", nativeQuery = true)
    List<String> findDistinctPublicCategoriesNonExpired();
    
    // Find distinct staff categories (non-expired)
    @Query(value = "SELECT DISTINCT Category FROM BankNews WHERE IsPublic = 0 AND (ExpiryDate IS NULL OR ExpiryDate > GETDATE()) ORDER BY Category", nativeQuery = true)
    List<String> findDistinctStaffCategoriesNonExpired();
    
    // Find news posted by a specific user (non-expired)
    @Query(value = "SELECT * FROM BankNews WHERE PostedBy = ?1 AND (ExpiryDate IS NULL OR ExpiryDate > GETDATE()) ORDER BY PostedAt DESC", nativeQuery = true)
    List<BankNews> findByPostedByAndNonExpiredOrderByPostedAtDesc(Integer userId);
    
    // Count non-expired news
    @Query(value = "SELECT COUNT(*) FROM BankNews WHERE (ExpiryDate IS NULL OR ExpiryDate > GETDATE())", nativeQuery = true)
    long countNonExpiredNews();
    
    // Count non-expired public news
    @Query(value = "SELECT COUNT(*) FROM BankNews WHERE IsPublic = 1 AND (ExpiryDate IS NULL OR ExpiryDate > GETDATE())", nativeQuery = true)
    long countNonExpiredPublicNews();
    
    // Count non-expired staff news
    @Query(value = "SELECT COUNT(*) FROM BankNews WHERE IsPublic = 0 AND (ExpiryDate IS NULL OR ExpiryDate > GETDATE())", nativeQuery = true)
    long countNonExpiredStaffNews();
    
    // Find recent news (last 30 days, non-expired)
    @Query(value = "SELECT * FROM BankNews WHERE PostedAt >= DATEADD(day, -30, GETDATE()) AND (ExpiryDate IS NULL OR ExpiryDate > GETDATE()) ORDER BY PostedAt DESC", nativeQuery = true)
    List<BankNews> findRecentNonExpiredNews();
    
    // Find recent public news (last 30 days, non-expired)
    @Query(value = "SELECT * FROM BankNews WHERE PostedAt >= DATEADD(day, -30, GETDATE()) AND IsPublic = 1 AND (ExpiryDate IS NULL OR ExpiryDate > GETDATE()) ORDER BY PostedAt DESC", nativeQuery = true)
    List<BankNews> findRecentNonExpiredPublicNews();
    
    // Find recent staff news (last 30 days, non-expired)
    @Query(value = "SELECT * FROM BankNews WHERE PostedAt >= DATEADD(day, -30, GETDATE()) AND IsPublic = 0 AND (ExpiryDate IS NULL OR ExpiryDate > GETDATE()) ORDER BY PostedAt DESC", nativeQuery = true)
    List<BankNews> findRecentNonExpiredStaffNews();
    
    // Get distinct categories for admin (all news)
    @Query("SELECT DISTINCT n.category FROM BankNews n ORDER BY n.category")
    List<String> findDistinctCategoriesForAdmin();
    
    // Get all news ordered by posted date (for admin)
    List<BankNews> findAllByOrderByPostedAtDesc();
    
    // Count active news
    @Query("SELECT COUNT(n) FROM BankNews n WHERE n.expiryDate IS NULL OR n.expiryDate >= CURRENT_DATE")
    long countActiveNews();
    
    // Count expired news
    @Query("SELECT COUNT(n) FROM BankNews n WHERE n.expiryDate IS NOT NULL AND n.expiryDate < CURRENT_DATE")
    long countExpiredNews();
    
    // Count public news
    @Query("SELECT COUNT(n) FROM BankNews n WHERE n.isPublic = true")
    long countPublicNews();
    
    // Count staff news
    @Query("SELECT COUNT(n) FROM BankNews n WHERE n.isPublic = false")
    long countStaffNews();
}

package com.ceylonbank.webbasedbankingsystem.repository;

import com.ceylonbank.webbasedbankingsystem.entity.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Integer> {
    
    // Find all open tickets
    List<SupportTicket> findByStatusOrderByCreatedAtDesc(String status);
    
    // Find all resolved tickets
    List<SupportTicket> findByStatusOrderByResolvedAtDesc(String status);
    
    // Find tickets by user
    List<SupportTicket> findByUserUserIdOrderByCreatedAtDesc(Integer userId);
    
    // Find tickets assigned to a specific CSE
    List<SupportTicket> findByAssignedToUserIdOrderByCreatedAtDesc(Integer assignedToUserId);
    
    // Count open tickets
    long countByStatus(String status);
    
    // Count resolved tickets today
    @Query(value = "SELECT COUNT(*) FROM SupportTickets t WHERE t.Status = 'Resolved' AND CAST(t.ResolvedAt AS DATE) = :date", nativeQuery = true)
    long countResolvedToday(@Param("date") LocalDate date);
    
    // Count open tickets today
    @Query(value = "SELECT COUNT(*) FROM SupportTickets t WHERE t.Status = 'Open' AND CAST(t.CreatedAt AS DATE) = :date", nativeQuery = true)
    long countOpenToday(@Param("date") LocalDate date);
    
    // Find tickets created today
    @Query(value = "SELECT * FROM SupportTickets t WHERE CAST(t.CreatedAt AS DATE) = :date ORDER BY t.CreatedAt DESC", nativeQuery = true)
    List<SupportTicket> findTicketsCreatedToday(@Param("date") LocalDate date);
    
    // Find tickets resolved today
    @Query(value = "SELECT * FROM SupportTickets t WHERE t.Status = 'Resolved' AND CAST(t.ResolvedAt AS DATE) = :date ORDER BY t.ResolvedAt DESC", nativeQuery = true)
    List<SupportTicket> findTicketsResolvedToday(@Param("date") LocalDate date);
    
    // Find tickets by status and date range
    @Query("SELECT t FROM SupportTicket t WHERE t.status = :status AND t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    List<SupportTicket> findByStatusAndDateRange(@Param("status") String status, 
                                                @Param("startDate") LocalDateTime startDate, 
                                                @Param("endDate") LocalDateTime endDate);
    
    // Find all tickets ordered by creation date
    List<SupportTicket> findAllByOrderByCreatedAtDesc();
    
    // Find tickets by subject containing text
    List<SupportTicket> findBySubjectContainingIgnoreCaseOrderByCreatedAtDesc(String subject);
    
    // Find tickets by message containing text
    List<SupportTicket> findByMessageContainingIgnoreCaseOrderByCreatedAtDesc(String message);
    
    // Find ticket by ID with user eagerly loaded
    @Query("SELECT t FROM SupportTicket t LEFT JOIN FETCH t.user WHERE t.ticketId = :ticketId")
    Optional<SupportTicket> findByIdWithUser(@Param("ticketId") Integer ticketId);
    
    // Delete all support tickets by user ID
    void deleteByUserUserId(Integer userId);
    
}

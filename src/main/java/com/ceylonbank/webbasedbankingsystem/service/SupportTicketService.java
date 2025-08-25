package com.ceylonbank.webbasedbankingsystem.service;

import com.ceylonbank.webbasedbankingsystem.entity.SupportTicket;
import com.ceylonbank.webbasedbankingsystem.entity.User;
import com.ceylonbank.webbasedbankingsystem.repository.SupportTicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class SupportTicketService {
    
    private static final Logger logger = LoggerFactory.getLogger(SupportTicketService.class);
    
    @Autowired
    private SupportTicketRepository supportTicketRepository;
    
    @Autowired
    private UserService userService;
    
    // Create a new support ticket
    public SupportTicket createTicket(User user, String subject, String message) {
        try {
            SupportTicket ticket = new SupportTicket(user, subject, message);
            SupportTicket savedTicket = supportTicketRepository.save(ticket);
            logger.info("Created new support ticket with ID: {} for user: {}", savedTicket.getTicketId(), user.getUserId());
            return savedTicket;
        } catch (Exception e) {
            logger.error("Error creating support ticket for user {}: {}", user.getUserId(), e.getMessage());
            throw new RuntimeException("Failed to create support ticket", e);
        }
    }
    
    // Get all open tickets
    public List<SupportTicket> getOpenTickets() {
        try {
            return supportTicketRepository.findByStatusOrderByCreatedAtDesc("Open");
        } catch (Exception e) {
            logger.error("Error fetching open tickets: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch open tickets", e);
        }
    }
    
    // Get all resolved tickets
    public List<SupportTicket> getResolvedTickets() {
        try {
            return supportTicketRepository.findByStatusOrderByResolvedAtDesc("Resolved");
        } catch (Exception e) {
            logger.error("Error fetching resolved tickets: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch resolved tickets", e);
        }
    }
    
    // Get tickets resolved today
    public List<SupportTicket> getTicketsResolvedToday() {
        try {
            return supportTicketRepository.findTicketsResolvedToday(LocalDate.now());
        } catch (Exception e) {
            logger.error("Error fetching tickets resolved today: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch tickets resolved today", e);
        }
    }
    
    // Get tickets created today
    public List<SupportTicket> getTicketsCreatedToday() {
        try {
            return supportTicketRepository.findTicketsCreatedToday(LocalDate.now());
        } catch (Exception e) {
            logger.error("Error fetching tickets created today: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch tickets created today", e);
        }
    }
    
    // Get ticket by ID
    public Optional<SupportTicket> getTicketById(Integer ticketId) {
        try {
            return supportTicketRepository.findByIdWithUser(ticketId);
        } catch (Exception e) {
            logger.error("Error fetching ticket with ID {}: {}", ticketId, e.getMessage());
            throw new RuntimeException("Failed to fetch ticket", e);
        }
    }
    
    // Mark ticket as resolved
    public SupportTicket resolveTicket(Integer ticketId, Integer resolvedByUserId) {
        try {
            Optional<SupportTicket> ticketOpt = supportTicketRepository.findById(ticketId);
            if (ticketOpt.isEmpty()) {
                throw new RuntimeException("Ticket not found with ID: " + ticketId);
            }
            
            SupportTicket ticket = ticketOpt.get();
            if (ticket.isResolved()) {
                throw new RuntimeException("Ticket is already resolved");
            }
            
            // Assign the ticket to the CSE who resolved it
            Optional<User> cseOpt = userService.getUserById(resolvedByUserId);
            if (cseOpt.isPresent()) {
                ticket.setAssignedTo(cseOpt.get());
            }
            
            ticket.markAsResolved();
            SupportTicket savedTicket = supportTicketRepository.save(ticket);
            
            logger.info("Resolved ticket with ID: {} by user: {}", ticketId, resolvedByUserId);
            return savedTicket;
        } catch (Exception e) {
            logger.error("Error resolving ticket with ID {}: {}", ticketId, e.getMessage());
            throw new RuntimeException("Failed to resolve ticket", e);
        }
    }
    
    // Assign ticket to CSE
    public SupportTicket assignTicket(Integer ticketId, Integer assignedToUserId) {
        try {
            Optional<SupportTicket> ticketOpt = supportTicketRepository.findById(ticketId);
            if (ticketOpt.isEmpty()) {
                throw new RuntimeException("Ticket not found with ID: " + ticketId);
            }
            
            Optional<User> cseOpt = userService.getUserById(assignedToUserId);
            if (cseOpt.isEmpty()) {
                throw new RuntimeException("CSE not found with ID: " + assignedToUserId);
            }
            
            SupportTicket ticket = ticketOpt.get();
            ticket.setAssignedTo(cseOpt.get());
            SupportTicket savedTicket = supportTicketRepository.save(ticket);
            
            logger.info("Assigned ticket with ID: {} to CSE: {}", ticketId, assignedToUserId);
            return savedTicket;
        } catch (Exception e) {
            logger.error("Error assigning ticket with ID {} to CSE {}: {}", ticketId, assignedToUserId, e.getMessage());
            throw new RuntimeException("Failed to assign ticket", e);
        }
    }
    
    // Get dashboard statistics
    public Map<String, Object> getDashboardStats() {
        try {
            LocalDate today = LocalDate.now();
            
            long openTickets = supportTicketRepository.countByStatus("Open");
            long resolvedToday = supportTicketRepository.countResolvedToday(today);
            long openToday = supportTicketRepository.countOpenToday(today);
            
            return Map.of(
                "openTickets", openTickets,
                "resolvedToday", resolvedToday,
                "openToday", openToday
            );
        } catch (Exception e) {
            logger.error("Error fetching dashboard statistics: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch dashboard statistics", e);
        }
    }
    
    
    // Get all tickets for admin view
    public List<SupportTicket> getAllTickets() {
        try {
            return supportTicketRepository.findAllByOrderByCreatedAtDesc();
        } catch (Exception e) {
            logger.error("Error fetching all tickets: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch all tickets", e);
        }
    }
    
    // Get tickets by user ID
    public List<SupportTicket> getTicketsByUserId(Integer userId) {
        try {
            return supportTicketRepository.findByUserUserIdOrderByCreatedAtDesc(userId);
        } catch (Exception e) {
            logger.error("Error fetching tickets for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to fetch user tickets", e);
        }
    }
    
    // Delete ticket (permanent deletion)
    public boolean deleteTicket(Integer ticketId) {
        try {
            Optional<SupportTicket> ticketOpt = supportTicketRepository.findById(ticketId);
            if (ticketOpt.isEmpty()) {
                throw new RuntimeException("Ticket not found with ID: " + ticketId);
            }
            
            SupportTicket ticket = ticketOpt.get();
            
            // Only allow deletion of resolved tickets
            if (!"Resolved".equals(ticket.getStatus())) {
                throw new RuntimeException("Only resolved tickets can be deleted");
            }
            
            supportTicketRepository.delete(ticket);
            logger.info("Deleted ticket with ID: {}", ticketId);
            return true;
        } catch (Exception e) {
            logger.error("Error deleting ticket with ID {}: {}", ticketId, e.getMessage());
            throw new RuntimeException("Failed to delete ticket", e);
        }
    }
}

package com.ceylonbank.webbasedbankingsystem.util;

import com.ceylonbank.webbasedbankingsystem.entity.AuditLog;
import com.ceylonbank.webbasedbankingsystem.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 Singleton AuditLogger class for centralized audit logging across the banking system.
 This class implements the Singleton design pattern to ensure that only one instance
 of the AuditLogger exists throughout the application lifecycle. This provides several benefits:
 *Single Point of Control: All audit logging operations go through one centralized instance
 *Resource Efficiency:Avoids creating multiple instances, reducing memory usage
 *Consistent Logging:Ensures uniform audit log format and behavior across the system
 *Thread Safety:The Singleton instance is thread-safe for concurrent access


 The Singleton pattern is implemented using the "Lazy Initialization with Double-Checked Locking"
 approach to ensure thread safety while maintaining performance.
 */
@Component
public class AuditLogger {
    
    /**
     * Private static volatile instance variable.
     * Volatile ensures that the instance is properly initialized when accessed from multiple threads.
     */
    private static volatile AuditLogger instance;
    
    /**
     * Spring-managed AuditLogRepository for database operations.
     * This is injected by Spring's dependency injection container.
     */
    private static AuditLogRepository auditLogRepository;
    
    /**
     * Private constructor to prevent external instantiation.
     * This is a key aspect of the Singleton pattern - only the class itself can create instances.
     */
    private AuditLogger() {
        // Private constructor prevents instantiation from outside the class
    }
    
    /**
     * Sets the AuditLogRepository instance.
     * This method is called by Spring's dependency injection to inject the repository.
     * 
     * @param repository the AuditLogRepository instance to be injected
     */
    @Autowired
    public void setAuditLogRepository(AuditLogRepository repository) {
        AuditLogger.auditLogRepository = repository;
    }
    
    /**
     * Returns the single instance of AuditLogger using thread-safe lazy initialization.

     *This method implements the "Double-Checked Locking" pattern to ensure thread safety
      while maintaining performance. The first check (without synchronization) is for performance,
      and the second check (with synchronization) ensures only one instance is created.</p>

      @return the single instance of AuditLogger
     */
    public static AuditLogger getInstance() {
        // First check (without synchronization) for better performance
        if (instance == null) {
            // Synchronize only when creating the instance
            synchronized (AuditLogger.class) {
                // Second check (with synchronization) to ensure thread safety
                if (instance == null) {
                    instance = new AuditLogger();
                }
            }
        }
        return instance;
    }
    
    /**
     * Logs an audit action to the database.

     *This method creates a new audit log entry and saves it to the database using
     the existing AuditLog entity and repository. The timestamp is automatically set
     by the entity's @PrePersist method.

     */
    public void logAction(Integer userId, String action, String details) {
        // Validate input parameters
        if (action == null || action.trim().isEmpty()) {
            throw new IllegalArgumentException("Action cannot be null or empty");
        }
        
        // Ensure repository is initialized
        if (auditLogRepository == null) {
            throw new IllegalStateException("AuditLogRepository is not initialized. " +
                    "Make sure AuditLogger is properly configured with Spring.");
        }
        
        try {
            // Create new audit log entry
            AuditLog auditLog = new AuditLog();
            auditLog.setUserId(userId);
            auditLog.setAction(action.trim());
            auditLog.setDetails(details != null ? details.trim() : null);
            // Timestamp will be automatically set by @PrePersist method in AuditLog entity
            
            // Save to database
            auditLogRepository.save(auditLog);
            
        } catch (Exception e) {
            // Log the error but don't throw it to avoid breaking the main application flow
            System.err.println("Failed to log audit action: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Logs an audit action with current timestamp information.
     * 
     * <p>This is a convenience method that automatically adds timestamp information
     * to the details field for better audit trail clarity.</p>
     * 
     * @param userId the ID of the user who performed the action
     * @param action the type of action performed
     * @param details detailed description of the action performed
     */
    public void logActionWithTimestamp(Integer userId, String action, String details) {
        String timestampedDetails = details + " [Logged at: " + LocalDateTime.now() + "]";
        logAction(userId, action, timestampedDetails);
    }
    
    /**
     * Logs a system action (when userId is not applicable).
     * 
     * <p>This method is used for logging system-level actions where no specific user
     * is involved, such as system startup, scheduled tasks, or automated processes.</p>
     * 
     * @param action the type of system action performed
     * @param details detailed description of the system action
     */
    public void logSystemAction(String action, String details) {
        logAction(null, "SYSTEM_" + action, details);
    }
    
    /**
     * Checks if the AuditLogger is properly initialized.
     * @return true if the repository is initialized and ready for use, false otherwise
     */
    public boolean isInitialized() {
        return auditLogRepository != null;
    }
}

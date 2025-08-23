package com.ceylonbank.webbasedbankingsystem.security;

import com.ceylonbank.webbasedbankingsystem.util.AuditLogger;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Authentication Failure Handler that logs failed login attempts to the audit log.
 * 
 * <p>This handler is triggered whenever a user fails to authenticate with the system.
 * It captures failed login attempts and logs them using the AuditLogger Singleton for security auditing purposes.</p>
 * 
 * <p>The handler logs:</p>
 * <ul>
 *   <li>User ID: null (since authentication failed)</li>
 *   <li>Action type: "LOGIN_FAILED"</li>
 *   <li>Details: Username and failure reason</li>
 * </ul>
 */
@Component
public class AuditAuthenticationFailureHandler implements AuthenticationFailureHandler {

    /**
     * Called when authentication fails.
     * 
     * @param request the HTTP request
     * @param response the HTTP response
     * @param exception the authentication exception that occurred
     * @throws IOException if an I/O error occurs
     * @throws ServletException if a servlet error occurs
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, 
                                       HttpServletResponse response, 
                                       AuthenticationException exception) throws IOException, ServletException {
        
        // Get the username from the request
        String username = request.getParameter("username");
        if (username == null) {
            username = "unknown";
        }
        
        System.out.println("Authentication failure for username: " + username);
        System.out.println("Exception message: " + (exception != null ? exception.getMessage() : "null"));
        
        // Log the failed login attempt to audit trail
        try {
            // Use the Singleton AuditLogger to log the failed login event
            // userId is null since authentication failed
            AuditLogger.getInstance().logAction(
                null,
                "LOGIN_FAILED",
                "Failed login attempt for username: " + username + 
                " - Reason: " + (exception != null ? exception.getMessage() : "Unknown error")
            );
        } catch (Exception e) {
            // Log error but don't break the authentication flow
            System.err.println("Failed to log authentication failure: " + e.getMessage());
        }
        
        // Check if the account is deactivated
        if (exception instanceof AccountDeactivatedException || 
            (exception != null && exception.getMessage() != null && 
             exception.getMessage().contains("Account is deactivated"))) {
            System.out.println("Redirecting to login with deactivated=true");
            // Redirect to login page with deactivated parameter
            response.sendRedirect("/login?deactivated=true");
        } else {
            System.out.println("Redirecting to login with error=true");
            // Redirect to login page with error parameter
            response.sendRedirect("/login?error=true");
        }
    }
}

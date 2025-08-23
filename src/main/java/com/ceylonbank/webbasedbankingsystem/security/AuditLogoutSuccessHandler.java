package com.ceylonbank.webbasedbankingsystem.security;

import com.ceylonbank.webbasedbankingsystem.util.AuditLogger;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Logout Success Handler that logs successful logout attempts to the audit log.
 * 
 * <p>This handler is triggered whenever a user successfully logs out of the system.
 * It captures the logout event and logs it using the AuditLogger Singleton for audit trail purposes.</p>
 * 
 * <p>The handler logs:</p>
 * <ul>
 *   <li>User ID of the user who logged out</li>
 *   <li>Action type: "LOGOUT"</li>
 *   <li>Details: Username and logout timestamp</li>
 * </ul>
 * 
 * <p>Note: The authentication object may be null if the user's session has already expired,
 * so we handle this case gracefully.</p>
 */
@Component
public class AuditLogoutSuccessHandler implements LogoutSuccessHandler {

    /**
     * Called when logout is successful.
     * 
     * @param request the HTTP request
     * @param response the HTTP response
     * @param authentication the authentication object (may be null if session expired)
     * @throws IOException if an I/O error occurs
     * @throws ServletException if a servlet error occurs
     */
    @Override
    public void onLogoutSuccess(HttpServletRequest request, 
                               HttpServletResponse response, 
                               Authentication authentication) throws IOException, ServletException {
        
        // Log the logout event to audit trail
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            
            try {
                // Use the Singleton AuditLogger to log the logout event
                AuditLogger.getInstance().logAction(
                    userDetails.getUserId(),
                    "LOGOUT",
                    "User " + userDetails.getUsername() + " successfully logged out of the system"
                );
            } catch (Exception e) {
                // Log error but don't break the logout flow
                System.err.println("Failed to log logout success: " + e.getMessage());
            }
        } else {
            // Handle case where authentication is null (session expired, etc.)
            try {
                // Try to get username from request parameters or session
                String username = "unknown";
                if (request.getSession() != null && request.getSession().getAttribute("username") != null) {
                    username = (String) request.getSession().getAttribute("username");
                }
                
                AuditLogger.getInstance().logAction(
                    null,
                    "LOGOUT",
                    "User " + username + " logged out (session may have expired)"
                );
            } catch (Exception e) {
                // Log error but don't break the logout flow
                System.err.println("Failed to log logout success (null auth): " + e.getMessage());
            }
        }
        
        // Redirect to home page after logout
        response.sendRedirect("/?logout=true");
    }
}

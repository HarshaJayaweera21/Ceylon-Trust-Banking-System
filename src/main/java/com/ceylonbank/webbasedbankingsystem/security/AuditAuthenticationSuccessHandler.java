package com.ceylonbank.webbasedbankingsystem.security;

import com.ceylonbank.webbasedbankingsystem.util.AuditLogger;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Authentication Success Handler that logs successful login attempts to the audit log.
 * 
 * <p>This handler is triggered whenever a user successfully authenticates with the system.
 * It captures the login event and logs it using the AuditLogger Singleton for audit trail purposes.</p>
 * 
 * <p>The handler logs:</p>
 * <ul>
 *   <li>User ID of the authenticated user</li>
 *   <li>Action type: "LOGIN"</li>
 *   <li>Details: Username and login timestamp</li>
 * </ul>
 */
@Component
public class AuditAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    /**
     * Called when authentication is successful.
     * 
     * @param request the HTTP request
     * @param response the HTTP response
     * @param authentication the successful authentication object
     * @throws IOException if an I/O error occurs
     * @throws ServletException if a servlet error occurs
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                      HttpServletResponse response, 
                                      Authentication authentication) throws IOException, ServletException {
        
        // Log the successful login to audit trail
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            
            try {
                // Use the Singleton AuditLogger to log the login event
                AuditLogger.getInstance().logAction(
                    userDetails.getUserId(),
                    "LOGIN",
                    "User " + userDetails.getUsername() + " successfully logged into the system"
                );
            } catch (Exception e) {
                // Log error but don't break the authentication flow
                System.err.println("Failed to log authentication success: " + e.getMessage());
            }
        }
        
        // Determine redirect URL based on user role
        String targetUrl = determineRedirectUrl(authentication);
        
        // Check for redirect parameter (for backward compatibility)
        String redirectUrl = request.getParameter("redirect");
        
        // Validate redirect URL to prevent open redirects
        if (redirectUrl != null && !redirectUrl.isEmpty() && 
            (redirectUrl.startsWith("/") || redirectUrl.startsWith(request.getContextPath()))) {
            // Clean the redirect URL
            if (redirectUrl.startsWith(request.getContextPath())) {
                redirectUrl = redirectUrl.substring(request.getContextPath().length());
            }
            response.sendRedirect(redirectUrl);
        } else {
            // Redirect based on role
            response.sendRedirect(request.getContextPath() + targetUrl);
        }
    }
    
    /**
     * Determines the appropriate redirect URL based on the user's role.
     * All users are redirected to the index page first, which handles role-based routing.
     * 
     * @param authentication the authentication object containing user authorities
     * @return the target URL for redirection (always "/" for index page)
     */
    private String determineRedirectUrl(Authentication authentication) {
        // All users are redirected to the index page first
        // The index page will handle role-based routing to appropriate dashboards
        return "/";
    }
}

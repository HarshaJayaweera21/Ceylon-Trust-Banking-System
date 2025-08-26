package com.ceylonbank.webbasedbankingsystem.controller;

import com.ceylonbank.webbasedbankingsystem.entity.User;
import com.ceylonbank.webbasedbankingsystem.security.CustomUserDetails;
import com.ceylonbank.webbasedbankingsystem.service.UserService;
import com.ceylonbank.webbasedbankingsystem.util.AuditLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserDetailsService userDetailsService;

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @PostMapping("/staff-login")
    public String handleStaffLogin(
            @RequestParam("userID") String userID,
            @RequestParam("roleID") String roleID,
            @RequestParam("nic") String nic,
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        
        try {
            // Validate staff credentials with additional fields
            User staffUser = userService.validateStaffLogin(userID, roleID, nic, username, password);
            
            if (staffUser != null) {
                // Load user details and authenticate
                CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(username);
                
                // Create authentication token with proper details
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                
                // Set authentication details
                authentication.setDetails(new WebAuthenticationDetails(request));
                
                // Set authentication in security context
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                // Store authentication in session
                HttpSession session = request.getSession(true);
                session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
                
                // Log the successful staff login to audit trail
                try {
                    AuditLogger.getInstance().logAction(
                        userDetails.getUserId(),
                        "STAFF_LOGIN",
                        "Staff user " + userDetails.getUsername() + " successfully logged into the system with role: " + staffUser.getRole().getRoleName()
                    );
                } catch (Exception e) {
                    System.err.println("Failed to log staff authentication success: " + e.getMessage());
                }
                
                // Redirect to home page after successful staff login
                return "redirect:/";
            } else {
                // Log failed staff login attempt
                try {
                    AuditLogger.getInstance().logAction(
                        null,
                        "STAFF_LOGIN_FAILED",
                        "Failed staff login attempt for username: " + username + " with UserID: " + userID
                    );
                } catch (Exception e) {
                    System.err.println("Failed to log staff authentication failure: " + e.getMessage());
                }
                
                redirectAttributes.addFlashAttribute("error", "Invalid staff credentials");
                return "redirect:/login?error=true";
            }
        } catch (Exception e) {
            // Log failed staff login attempt
            try {
                AuditLogger.getInstance().logAction(
                    null,
                    "STAFF_LOGIN_FAILED",
                    "Failed staff login attempt for username: " + username + " - Error: " + e.getMessage()
                );
            } catch (Exception ex) {
                System.err.println("Failed to log staff authentication failure: " + ex.getMessage());
            }
            
            redirectAttributes.addFlashAttribute("error", "Authentication failed: " + e.getMessage());
            return "redirect:/login?error=true";
        }
    }
}
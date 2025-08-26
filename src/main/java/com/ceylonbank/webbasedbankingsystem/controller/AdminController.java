package com.ceylonbank.webbasedbankingsystem.controller;

import com.ceylonbank.webbasedbankingsystem.dto.CustomerDetailsDTO;
import com.ceylonbank.webbasedbankingsystem.entity.User;
import com.ceylonbank.webbasedbankingsystem.repository.UserRepository;
import com.ceylonbank.webbasedbankingsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Controller
public class AdminController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/admin/customers")
    public String adminCustomersPage(@RequestParam(required = false) String q,
                                   @RequestParam(required = false) Boolean hideInactive,
                                   Model model) {
        // Get all customers (users with Customer role) - keep original list for statistics
        List<User> allCustomers = userService.getCustomers();
        List<User> customers = allCustomers;
        
        // Apply search filter
        if (q != null && !q.trim().isEmpty()) {
            customers = customers.stream()
                    .filter(customer -> 
                        customer.getUsername().toLowerCase().contains(q.toLowerCase()) ||
                        (customer.getFirstName() + " " + customer.getLastName()).toLowerCase().contains(q.toLowerCase()) ||
                        (customer.getNic() != null && customer.getNic().toLowerCase().contains(q.toLowerCase()))
                    )
                    .toList();
        }
        
        // Show all customers by default, checkbox controls visibility of inactive ones
        if (hideInactive != null && hideInactive) {
            // If hideInactive is true, show only active customers
            customers = customers.stream()
                    .filter(customer -> customer.getIsActive())
                    .toList();
        }
        // If hideInactive is false or null, show all customers (both active and inactive)
        
        model.addAttribute("customers", customers);
        model.addAttribute("allCustomers", allCustomers); // Add all customers for statistics
        model.addAttribute("q", q);
        model.addAttribute("hideInactive", hideInactive);
        
        return "admin-customers";
    }

    @GetMapping("/admin/customers/{id}")
    public ResponseEntity<?> viewCustomer(@PathVariable Integer id) {
        Optional<User> customer = userService.getUserById(id);
        if (customer.isPresent()) {
            User user = customer.get();
            CustomerDetailsDTO dto = new CustomerDetailsDTO(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getNic(),
                user.getFirstName(),
                user.getLastName(),
                user.getDateOfBirth() != null ? user.getDateOfBirth().format(DateTimeFormatter.ISO_LOCAL_DATE) : null,
                user.getStreet(),
                user.getCity(),
                user.getPostalCode(),
                user.getIsActive(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getRole() != null ? user.getRole().getRoleName() : null
            );
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/admin/customers/{id}/deactivate")
    public ResponseEntity<?> deactivateCustomer(@PathVariable Integer id) {
        System.out.println("Deactivate customer endpoint called with ID: " + id);
        try {
            userService.deactivateUser(id);
            System.out.println("Customer deactivated successfully: " + id);
            return ResponseEntity.ok().body("Customer deactivated successfully");
        } catch (Exception e) {
            System.out.println("Error deactivating customer " + id + ": " + e.getMessage());
            return ResponseEntity.badRequest().body("Failed to deactivate customer: " + e.getMessage());
        }
    }

    @PostMapping("/admin/customers/{id}/activate")
    public ResponseEntity<?> activateCustomer(@PathVariable Integer id) {
        System.out.println("Activate customer endpoint called with ID: " + id);
        try {
            userService.activateUser(id);
            System.out.println("Customer activated successfully: " + id);
            return ResponseEntity.ok().body("Customer activated successfully");
        } catch (Exception e) {
            System.out.println("Error activating customer " + id + ": " + e.getMessage());
            return ResponseEntity.badRequest().body("Failed to activate customer: " + e.getMessage());
        }
    }
    
    @GetMapping("/admin/test-user-status/{username}")
    public ResponseEntity<?> testUserStatus(@PathVariable String username) {
        try {
            Optional<User> user = userService.getUserById(userRepository.findByUsername(username).get().getUserId());
            if (user.isPresent()) {
                User u = user.get();
                return ResponseEntity.ok().body("User: " + u.getUsername() + 
                    ", isActive: " + u.getIsActive() + 
                    ", isActive type: " + (u.getIsActive() != null ? u.getIsActive().getClass().getSimpleName() : "null"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}

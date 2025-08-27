package com.ceylonbank.webbasedbankingsystem.controller;

import com.ceylonbank.webbasedbankingsystem.dto.StaffDetailsDTO;
import com.ceylonbank.webbasedbankingsystem.entity.Role;
import com.ceylonbank.webbasedbankingsystem.entity.User;
import com.ceylonbank.webbasedbankingsystem.entity.UserPhone;
import com.ceylonbank.webbasedbankingsystem.repository.RoleRepository;
import com.ceylonbank.webbasedbankingsystem.repository.UserPhoneRepository;
import com.ceylonbank.webbasedbankingsystem.repository.UserRepository;
import com.ceylonbank.webbasedbankingsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class StaffController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private UserPhoneRepository userPhoneRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/admin/staff")
    public String adminStaffPage(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer roleId,
            @RequestParam(required = false, defaultValue = "false") Boolean hideInactive,
            Model model,
            Authentication authentication) {
        
        System.out.println("Admin staff page called with q=" + q + ", roleId=" + roleId + ", hideInactive=" + hideInactive);
        
        // Get all staff for statistics
        List<User> allStaff = userService.getStaff();
        model.addAttribute("allStaff", allStaff);
        
        // Get current logged-in user
        String currentUsername = authentication != null ? authentication.getName() : null;
        System.out.println("Current logged-in user: " + currentUsername);
        
        // Filter staff based on criteria and exclude current user
        List<User> staff = allStaff.stream()
                .filter(user -> {
                    // Exclude current logged-in user
                    if (currentUsername != null && user.getUsername().equals(currentUsername)) {
                        return false;
                    }
                    
                    // Search filter
                    if (q != null && !q.trim().isEmpty()) {
                        String searchTerm = q.toLowerCase();
                        boolean matchesSearch = 
                            user.getUsername().toLowerCase().contains(searchTerm) ||
                            user.getFirstName().toLowerCase().contains(searchTerm) ||
                            user.getLastName().toLowerCase().contains(searchTerm) ||
                            (user.getNic() != null && user.getNic().toLowerCase().contains(searchTerm));
                        if (!matchesSearch) return false;
                    }
                    
                    // Role filter - using correct role IDs from database
                    if (roleId != null) {
                        if (user.getRole() == null || !user.getRole().getRoleId().equals(roleId)) {
                            return false;
                        }
                    }
                    
                    // Active status filter
                    if (hideInactive && (user.getIsActive() == null || !user.getIsActive())) {
                        return false;
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
        
        // Get all roles for the edit modal
        List<Role> roles = roleRepository.findAll().stream()
                .filter(role -> !role.getRoleName().equals("Customer"))
                .collect(Collectors.toList());

        model.addAttribute("staff", staff);
        model.addAttribute("allStaff", allStaff);
        model.addAttribute("roles", roles);
        model.addAttribute("q", q);
        model.addAttribute("roleId", roleId);
        model.addAttribute("hideInactive", hideInactive);

        System.out.println("Found " + staff.size() + " staff members");

        return "admin-staff-list";
    }

    @GetMapping("/admin/staff/{id}")
    public ResponseEntity<?> viewStaff(@PathVariable Integer id) {
        System.out.println("View staff endpoint called with ID: " + id);
        Optional<User> staff = userService.getUserById(id);
        if (staff.isPresent()) {
            User user = staff.get();
                       StaffDetailsDTO dto = new StaffDetailsDTO(
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
                           user.getRole() != null ? user.getRole().getRoleName() : null,
                           user.getRole() != null ? user.getRole().getRoleId() : null
                       );
            System.out.println("Returning staff details for: " + user.getUsername());
            return ResponseEntity.ok(dto);
        }
        System.out.println("Staff not found with ID: " + id);
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/admin/staff/{id}/deactivate")
    public ResponseEntity<?> deactivateStaff(@PathVariable Integer id) {
        System.out.println("Deactivate staff endpoint called with ID: " + id);
        try {
            userService.deactivateUser(id);
            System.out.println("Staff deactivated successfully: " + id);
            return ResponseEntity.ok().body("Staff member deactivated successfully");
        } catch (Exception e) {
            System.out.println("Error deactivating staff " + id + ": " + e.getMessage());
            return ResponseEntity.badRequest().body("Failed to deactivate staff member: " + e.getMessage());
        }
    }

    @PostMapping("/admin/staff/{id}/activate")
    public ResponseEntity<?> activateStaff(@PathVariable Integer id) {
        System.out.println("Activate staff endpoint called with ID: " + id);
        try {
            userService.activateUser(id);
            System.out.println("Staff activated successfully: " + id);
            return ResponseEntity.ok().body("Staff member activated successfully");
        } catch (Exception e) {
            System.out.println("Error activating staff " + id + ": " + e.getMessage());
            return ResponseEntity.badRequest().body("Failed to activate staff member: " + e.getMessage());
        }
    }

    @GetMapping("/admin/staff/{id}/edit")
    public String editStaff(@PathVariable Integer id, Model model) {
        System.out.println("Edit staff page called with ID: " + id);
        Optional<User> staff = userService.getUserById(id);
        if (staff.isPresent()) {
            model.addAttribute("user", staff.get());
            System.out.println("Staff found for editing: " + staff.get().getUsername());
            return "admin-staff-edit";
        } else {
            System.out.println("Staff not found with ID: " + id);
            return "redirect:/admin/staff?error=Staff not found";
        }
    }

    @PostMapping("/admin/staff/{id}/edit")
    public String updateStaff(@PathVariable Integer id,
                             @RequestParam String email,
                             @RequestParam String firstName,
                             @RequestParam String lastName,
                             @RequestParam(required = false) String dateOfBirth,
                             @RequestParam(required = false) String street,
                             @RequestParam(required = false) String city,
                             @RequestParam(required = false) String postalCode,
                             @RequestParam(required = false) Integer roleId,
                             Model model) {
        System.out.println("Update staff called with ID: " + id);
        try {
            Optional<User> staffOptional = userService.getUserById(id);
            if (staffOptional.isPresent()) {
                User staff = staffOptional.get();

                // Update staff information
                staff.setEmail(email);
                staff.setFirstName(firstName);
                staff.setLastName(lastName);

                if (dateOfBirth != null && !dateOfBirth.isEmpty()) {
                    staff.setDateOfBirth(java.time.LocalDate.parse(dateOfBirth));
                }

                staff.setStreet(street);
                staff.setCity(city);
                staff.setPostalCode(postalCode);

                // Update role if provided
                if (roleId != null) {
                    Optional<Role> roleOptional = roleRepository.findById(roleId);
                    if (roleOptional.isPresent()) {
                        staff.setRole(roleOptional.get());
                        System.out.println("Role updated to: " + roleOptional.get().getRoleName());
                    }
                }

                // Update the user in database
                userRepository.save(staff);
                
                System.out.println("Staff updated successfully: " + staff.getUsername());
                return "redirect:/admin/staff?success=Staff updated successfully";
            } else {
                System.out.println("Staff not found with ID: " + id);
                return "redirect:/admin/staff?error=Staff not found";
            }
        } catch (Exception e) {
            System.out.println("Error updating staff " + id + ": " + e.getMessage());
            return "redirect:/admin/staff?error=Failed to update staff: " + e.getMessage();
        }
    }

    @GetMapping("/admin/staff/register")
    public String staffRegisterPage(Model model) {
        System.out.println("Staff registration page called");
        
        // Get all roles except Customer (role ID 1)
        List<Role> roles = roleRepository.findAll().stream()
                .filter(role -> !role.getRoleName().equals("Customer"))
                .collect(Collectors.toList());
        
        model.addAttribute("roles", roles);
        model.addAttribute("staff", new User()); // Empty user object for form binding
        
        System.out.println("Found " + roles.size() + " roles for staff registration");
        return "staff-register";
    }
    
    @PostMapping("/admin/staff/register")
    public ResponseEntity<?> registerStaff(@RequestParam String firstName,
                                         @RequestParam String lastName,
                                         @RequestParam String email,
                                         @RequestParam String phone,
                                         @RequestParam String nic,
                                         @RequestParam(required = false) Integer roleId,
                                         @RequestParam String username,
                                         @RequestParam String password,
                                         @RequestParam String confirmPassword,
                                         @RequestParam String street,
                                         @RequestParam String city,
                                         @RequestParam String postalCode,
                                         @RequestParam String dateOfBirth) {
        System.out.println("Staff registration submitted for: " + username);
        
        try {
            // Validate password match
            if (!password.equals(confirmPassword)) {
                return ResponseEntity.badRequest().body("Passwords do not match");
            }
            
            // Check if username already exists
            if (userRepository.existsByUsername(username)) {
                return ResponseEntity.badRequest().body("Username already exists");
            }
            
            // Check if email already exists
            if (userRepository.existsByEmail(email)) {
                return ResponseEntity.badRequest().body("Email already exists");
            }
            
            // Check if NIC already exists
            if (userRepository.existsByNic(nic)) {
                return ResponseEntity.badRequest().body("NIC already exists");
            }
            
            // Create User object
            User staff = new User();
            staff.setUsername(username);
            staff.setEmail(email);
            staff.setNic(nic);
            staff.setFirstName(firstName);
            staff.setLastName(lastName);
            staff.setStreet(street);
            staff.setCity(city);
            staff.setPostalCode(postalCode);
            
            // Set date of birth
            if (dateOfBirth != null && !dateOfBirth.isEmpty()) {
                staff.setDateOfBirth(java.time.LocalDate.parse(dateOfBirth));
            }
            
            // Set role
            if (roleId != null) {
                Optional<Role> roleOptional = roleRepository.findById(roleId);
                if (roleOptional.isPresent()) {
                    staff.setRole(roleOptional.get());
                }
            }
            
            // Set default values
            staff.setIsActive(true);
            staff.setCreatedAt(LocalDateTime.now());
            staff.setUpdatedAt(LocalDateTime.now());
            
            // Encode password and set it
            System.out.println("Controller - encoding password: " + password);
            String encodedPassword = passwordEncoder.encode(password);
            staff.setPasswordHash(encodedPassword);
            System.out.println("Controller - password encoded and set to staff object");
            
            // Save staff member
            User savedStaff = userService.createUser(staff);
            
            // Save phone number
            if (phone != null && !phone.trim().isEmpty()) {
                UserPhone userPhone = new UserPhone();
                userPhone.setUser(savedStaff);
                userPhone.setPhoneNumber(phone);
                userPhoneRepository.save(userPhone);
            }
            
            System.out.println("Staff member registered successfully: " + savedStaff.getUsername());
            return ResponseEntity.ok("Staff member registered successfully");
            
        } catch (Exception e) {
            System.out.println("Error registering staff: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Registration failed: " + e.getMessage());
        }
    }
}

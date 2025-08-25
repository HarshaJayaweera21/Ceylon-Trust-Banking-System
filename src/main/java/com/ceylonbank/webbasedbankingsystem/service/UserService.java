package com.ceylonbank.webbasedbankingsystem.service;

import com.ceylonbank.webbasedbankingsystem.dto.UserRegistrationDto;
import com.ceylonbank.webbasedbankingsystem.entity.Role;
import com.ceylonbank.webbasedbankingsystem.entity.User;
import com.ceylonbank.webbasedbankingsystem.entity.UserPhone;
import com.ceylonbank.webbasedbankingsystem.repository.RoleRepository;
import com.ceylonbank.webbasedbankingsystem.repository.UserPhoneRepository;
import com.ceylonbank.webbasedbankingsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserPhoneRepository userPhoneRepository;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getCustomers() {
        return userRepository.findByRole_RoleName("Customer");
    }
    
    public List<User> getStaff() {
        // Get all users and filter out customers (role ID 1)
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() != null && !user.getRole().getRoleId().equals(1))
                .collect(Collectors.toList());
    }

    public Optional<User> getUserById(Integer id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> getUsersByRole(String roleName) {
        return userRepository.findByRole_RoleName(roleName);
    }

    @Transactional
    public User createUser(User user) {
        // Validate uniqueness
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (user.getNic() != null && userRepository.existsByNic(user.getNic())) {
            throw new IllegalArgumentException("NIC already exists");
        }

        // Password is already encoded in controller, no need to encode again
        System.out.println("UserService.createUser - passwordHash value: " + (user.getPasswordHash() != null ? "NOT NULL" : "NULL"));
        // Password is already encoded, so we don't encode it again
        if (user.getSecurityAnswer1() != null) {
            user.setSecurityAnswer1(passwordEncoder.encode(user.getSecurityAnswer1()));
        }
        if (user.getSecurityAnswer2() != null) {
            user.setSecurityAnswer2(passwordEncoder.encode(user.getSecurityAnswer2()));
        }

        // Handle phones using helper method
        List<UserPhone> phonesCopy = user.getPhones() != null ? new ArrayList<>(user.getPhones()) : new ArrayList<>();
        user.setPhones(new ArrayList<>()); // Clear to re-add safely
        for (UserPhone phone : phonesCopy) {
            if (phone.getPhoneNumber() != null && !phone.getPhoneNumber().isEmpty()) {
                // Validate phone number format (e.g., +94xxxxxxxxx)
                if (!phone.getPhoneNumber().matches("^\\+94\\d{9}$")) {
                    throw new IllegalArgumentException("Invalid phone number format: " + phone.getPhoneNumber());
                }
                user.addPhone(phone); // Sets bidirectional relationship
            }
        }

        // Save user (cascade saves phones)
        return userRepository.save(user);
    }

    public User createCustomer(User user) {
        System.out.println("createCustomer called for: " + user.getFirstName() + " " + user.getLastName());
        Optional<Role> customerRole = roleRepository.findByRoleName("Customer");
        if (customerRole.isEmpty()) {
            throw new IllegalArgumentException("Customer role not found");
        }
        user.setRole(customerRole.get());
        User createdUser = createUser(user);
        System.out.println("User created with ID: " + createdUser.getUserId());
        return createdUser;
    }

    @Transactional
    public User updateUser(Integer id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setFirstName(userDetails.getFirstName());
        user.setLastName(userDetails.getLastName());
        user.setEmail(userDetails.getEmail());
        user.setNic(userDetails.getNic());
        user.setStreet(userDetails.getStreet());
        user.setCity(userDetails.getCity());
        user.setPostalCode(userDetails.getPostalCode());
        user.setDateOfBirth(userDetails.getDateOfBirth());
        user.setSecurityQuestion1(userDetails.getSecurityQuestion1());
        user.setSecurityAnswer1(passwordEncoder.encode(userDetails.getSecurityAnswer1()));
        user.setSecurityQuestion2(userDetails.getSecurityQuestion2());
        user.setSecurityAnswer2(passwordEncoder.encode(userDetails.getSecurityAnswer2()));

        // Update phones
        userPhoneRepository.deleteByUserUserId(id);
        List<UserPhone> phonesCopy = userDetails.getPhones() != null ? new ArrayList<>(userDetails.getPhones()) : new ArrayList<>();
        user.setPhones(new ArrayList<>());
        for (UserPhone phone : phonesCopy) {
            if (phone.getPhoneNumber() != null && !phone.getPhoneNumber().isEmpty()) {
                // Validate phone number format
                if (!phone.getPhoneNumber().matches("^\\+94\\d{9}$")) {
                    throw new IllegalArgumentException("Invalid phone number format: " + phone.getPhoneNumber());
                }
                user.addPhone(phone); // Sets bidirectional relationship
            }
        }

        return userRepository.save(user);
    }

    @Transactional
    public User updateUserProfile(Integer id, String firstName, String lastName, String email, 
                                 String street, String city, String postalCode) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Update only the profile fields, don't touch phones or other relationships
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setStreet(street);
        user.setCity(city);
        user.setPostalCode(postalCode);
        
        return userRepository.save(user);
    }

    @Transactional
    public void deactivateUser(Integer id) {
        System.out.println("Attempting to deactivate user with ID: " + id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        System.out.println("Found user: " + user.getUsername());
        
        // Deactivate the user account instead of deleting
        user.setIsActive(false);
        userRepository.save(user);
        System.out.println("Deactivated user account for ID: " + id);
    }

    @Transactional
    public void activateUser(Integer id) {
        System.out.println("Attempting to activate user with ID: " + id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        System.out.println("Found user: " + user.getUsername());
        
        // Activate the user account
        user.setIsActive(true);
        userRepository.save(user);
        System.out.println("Activated user account for ID: " + id);
    }

    public boolean resetPassword(String username, String answer1, String answer2, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!passwordEncoder.matches(answer1, user.getSecurityAnswer1()) ||
                !passwordEncoder.matches(answer2, user.getSecurityAnswer2())) {
            throw new IllegalArgumentException("Security answers do not match");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }

    public Map<Integer, User> getUsersByIds(List<Integer> userIds) {
        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, user -> user));
    }
    
    public List<User> searchCustomers(String searchTerm) {
        // Search by NIC, first name, last name, or email
        List<User> results = new ArrayList<>();
        
        // Search by exact NIC first (highest priority)
        Optional<User> exactNicResult = userRepository.findByNic(searchTerm);
        if (exactNicResult.isPresent()) {
            results.add(exactNicResult.get());
        }
        
        // Search by partial NIC matching (for real-time search - starts with)
        results.addAll(userRepository.findByNicStartingWithIgnoreCase(searchTerm));
        
        // Search by first name
        results.addAll(userRepository.findByFirstNameContainingIgnoreCase(searchTerm));
        
        // Search by last name
        results.addAll(userRepository.findByLastNameContainingIgnoreCase(searchTerm));
        
        // Search by email
        Optional<User> emailResult = userRepository.findByEmail(searchTerm);
        if (emailResult.isPresent()) {
            results.add(emailResult.get());
        }
        
        // Remove duplicates and filter for customers only
        return results.stream()
                .distinct()
                .filter(user -> "Customer".equals(user.getRole().getRoleName()))
                .collect(Collectors.toList());
    }

    public List<User> getStaffUsers() {
        // Get all users with staff roles (excluding System Administrator and Customer)
        List<String> staffRoleNames = List.of("Cashier", "LoanOfficer", "CustomerServiceExecutive");
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() != null && staffRoleNames.contains(user.getRole().getRoleName()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void changeUserRole(Integer userId, String newRoleName) {
        // Find the user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Validate that the new role is a staff role
        List<String> allowedRoles = List.of("Cashier", "LoanOfficer", "CustomerServiceExecutive");
        if (!allowedRoles.contains(newRoleName)) {
            throw new IllegalArgumentException("Invalid role specified. Only staff roles are allowed.");
        }

        // Prevent changing to the same role
        if (newRoleName.equals(user.getRole().getRoleName())) {
            throw new IllegalArgumentException("User already has this role.");
        }

        // Find the new role
        Role newRole = roleRepository.findByRoleName(newRoleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + newRoleName));

        // Update the user's role
        user.setRole(newRole);
        userRepository.save(user);
    }

    /**
     * Validates staff login with additional security fields
     * @param userID Staff User ID
     * @param roleID Staff Role ID
     * @param nic Staff NIC number
     * @param username Staff username
     * @param password Staff password
     * @return User object if validation successful, null otherwise
     */
    public User validateStaffLogin(String userID, String roleID, String nic, String username, String password) {
        try {
            // Find user by username
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                return null;
            }

            User user = userOpt.get();

            // Validate password
            if (!passwordEncoder.matches(password, user.getPasswordHash())) {
                return null;
            }

            // Validate that user is active
            if (!user.getIsActive()) {
                return null;
            }

            // Validate that user has a staff role (not Customer)
            if (user.getRole() == null || "Customer".equals(user.getRole().getRoleName())) {
                return null;
            }

            // Additional validation for staff-specific fields
            // Check if the provided User ID matches the user's actual ID
            if (userID != null && !userID.trim().isEmpty()) {
                try {
                    Integer providedUserId = Integer.parseInt(userID.trim());
                    if (!providedUserId.equals(user.getUserId())) {
                        return null;
                    }
                } catch (NumberFormatException e) {
                    return null;
                }
            }

            // Check if the provided Role ID matches the user's role ID
            if (roleID != null && !roleID.trim().isEmpty()) {
                try {
                    Integer providedRoleId = Integer.parseInt(roleID.trim());
                    if (!providedRoleId.equals(user.getRole().getRoleId())) {
                        return null;
                    }
                } catch (NumberFormatException e) {
                    return null;
                }
            }

            // Check if the provided NIC matches the user's NIC
            if (nic != null && !nic.trim().isEmpty()) {
                if (!nic.trim().equals(user.getNic())) {
                    return null;
                }
            }

            return user;
        } catch (Exception e) {
            // Log the exception for debugging but don't expose it
            System.err.println("Staff login validation error: " + e.getMessage());
            return null;
        }
    }

    @Transactional
    public void registerUser(UserRegistrationDto dto) {
        // Validate uniqueness
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (dto.getNic() != null && userRepository.existsByNic(dto.getNic())) {
            throw new IllegalArgumentException("NIC already exists");
        }

        // Map DTO to entity
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setNic(dto.getNic());

        // Parse DD/MM/YYYY to LocalDate
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        user.setDateOfBirth(java.time.LocalDate.parse(dto.getDob(), fmt));

        String street = dto.getStreet();
        if (dto.getStreet2() != null && !dto.getStreet2().isBlank()) {
            street = street + ", " + dto.getStreet2();
        }
        user.setStreet(street);
        user.setCity(dto.getCity());
        user.setPostalCode(dto.getPostalCode());

        user.setSecurityQuestion1(dto.getSecurityQuestion1());
        user.setSecurityAnswer1(passwordEncoder.encode(dto.getSecurityAnswer1()));
        user.setSecurityQuestion2(dto.getSecurityQuestion2());
        user.setSecurityAnswer2(passwordEncoder.encode(dto.getSecurityAnswer2()));

        // Assign default Customer role
        Role customerRole = roleRepository.findByRoleName("Customer")
                .orElseThrow(() -> new IllegalArgumentException("Customer role not found"));
        user.setRole(customerRole);

        // Set defaults
        user.setIsActive(true);
        user.setCreatedAt(java.time.LocalDateTime.now());
        user.setUpdatedAt(java.time.LocalDateTime.now());

        // Save user first
        User savedUser = userRepository.save(user);

        // Phones - save after user is saved to get the user ID
        if (dto.getPhone() != null && !dto.getPhone().isEmpty()) {
            UserPhone phone = new UserPhone();
            phone.setPhoneNumber("+94" + dto.getPhone());
            phone.setUser(savedUser);
            userPhoneRepository.save(phone);
        }
    }
}
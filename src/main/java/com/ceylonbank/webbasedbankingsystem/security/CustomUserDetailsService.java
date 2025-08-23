package com.ceylonbank.webbasedbankingsystem.security;

import com.ceylonbank.webbasedbankingsystem.entity.User;
import com.ceylonbank.webbasedbankingsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("Loading user by username: " + username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        System.out.println("User found: " + user.getUsername() + ", isActive: " + user.getIsActive());
        
        // Check if account is active
        Boolean isActive = user.getIsActive();
        System.out.println("isActive value: " + isActive + " (type: " + (isActive != null ? isActive.getClass().getSimpleName() : "null") + ")");
        
        if (isActive == null || !isActive) {
            System.out.println("Account is deactivated for user: " + username);
            throw new AccountDeactivatedException("Account is deactivated");
        }
        
        System.out.println("User authentication successful: " + username);
        return new CustomUserDetails(user);
    }
}

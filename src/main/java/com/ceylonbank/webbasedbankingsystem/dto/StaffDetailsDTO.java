package com.ceylonbank.webbasedbankingsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffDetailsDTO {
    private Integer userId;
    private String username;
    private String email;
    private String nic;
    private String firstName;
    private String lastName;
    private String dob; // Formatted as String
    private String street;
    private String city;
    private String postalCode;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String roleName;
    private Integer roleId; // Added for role editing
}

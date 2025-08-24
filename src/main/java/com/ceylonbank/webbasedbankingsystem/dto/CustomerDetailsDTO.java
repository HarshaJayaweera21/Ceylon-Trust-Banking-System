package com.ceylonbank.webbasedbankingsystem.dto;

import java.time.LocalDateTime;

public class CustomerDetailsDTO {
    private Integer userId;
    private String username;
    private String email;
    private String nic;
    private String firstName;
    private String lastName;
    private String dob;
    private String street;
    private String city;
    private String postalCode;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String roleName;

    // Constructors
    public CustomerDetailsDTO() {}

    public CustomerDetailsDTO(Integer userId, String username, String email, String nic, 
                             String firstName, String lastName, String dob, String street, 
                             String city, String postalCode, Boolean isActive, 
                             LocalDateTime createdAt, LocalDateTime updatedAt, String roleName) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.nic = nic;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
        this.street = street;
        this.city = city;
        this.postalCode = postalCode;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.roleName = roleName;
    }

    // Getters and Setters
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNic() {
        return nic;
    }

    public void setNic(String nic) {
        this.nic = nic;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}

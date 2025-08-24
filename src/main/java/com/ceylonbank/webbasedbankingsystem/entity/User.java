//package com.ceylonbank.webbasedbankingsystem.entity;
//
//import jakarta.persistence.*;
//import lombok.Data;
//import org.hibernate.annotations.CreationTimestamp;
//import org.hibernate.annotations.UpdateTimestamp;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "Users", indexes = {
//        @Index(name = "IDX_Users_Username", columnList = "Username", unique = true),
//        @Index(name = "IDX_Users_Email", columnList = "Email", unique = true)
//})
//@Data
//public class User {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "UserID")
//    private Integer userId;
//
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "RoleID", nullable = false)
//    private Role role;
//
//    @Column(name = "Username", unique = true, nullable = false, length = 50)
//    private String username;
//
//    @Column(name = "PasswordHash", nullable = false, length = 255)
//    private String passwordHash;
//
//    @Column(name = "FullName", nullable = false, length = 100)
//    private String fullName;
//
//    @Column(name = "Email", unique = true, nullable = false, length = 100)
//    private String email;
//
//    @Column(name = "Phone", length = 20)
//    private String phone;
//
//    @Column(name = "NIC", unique = true, length = 20)
//    private String nic;
//
//    @Column(name = "Address", length = 200)
//    private String address;
//
//    @Column(name = "DOB")
//    private LocalDate dateOfBirth;
//
//    @Column(name = "SecurityQuestion1", length = 200)
//    private String securityQuestion1;
//
//    @Column(name = "SecurityAnswer1", length = 255)
//    private String securityAnswer1;
//
//    @Column(name = "SecurityQuestion2", length = 200)
//    private String securityQuestion2;
//
//    @Column(name = "SecurityAnswer2", length = 255)
//    private String securityAnswer2;
//
//    @Column(name = "IsActive", nullable = false)
//    private Boolean isActive = true;
//
//    @CreationTimestamp
//    @Column(name = "CreatedAt", updatable = false)
//    private LocalDateTime createdAt;
//
//    @UpdateTimestamp
//    @Column(name = "UpdatedAt")
//    private LocalDateTime updatedAt;
//
//    // Constructors
//    public User() {}
//
//    public User(String username, String passwordHash, String fullName, String email, Role role) {
//        this.username = username;
//        this.passwordHash = passwordHash;
//        this.fullName = fullName;
//        this.email = email;
//        this.role = role;
//    }
//}

package com.ceylonbank.webbasedbankingsystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Users", indexes = {
        @Index(name = "IDX_Users_Username", columnList = "Username", unique = true),
        @Index(name = "IDX_Users_Email", columnList = "Email", unique = true),
        @Index(name = "IDX_Users_NIC", columnList = "NIC", unique = true),
        @Index(name = "IDX_Users_Name", columnList = "LastName, FirstName")
})
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserID")
    private Integer userId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "RoleID", nullable = false)
    private Role role;

    @Column(name = "Username", unique = true, nullable = false, length = 50)
    private String username;

    @Column(name = "PasswordHash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "FirstName", nullable = false, length = 50)
    private String firstName;

    @Column(name = "LastName", nullable = false, length = 50)
    private String lastName;

    @Column(name = "Email", unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "NIC", unique = true, length = 20)
    private String nic;

    @Column(name = "Street", length = 100)
    private String street;

    @Column(name = "City", length = 50)
    private String city;

    @Column(name = "PostalCode", length = 20)
    private String postalCode;

    @Column(name = "DOB")
    private LocalDate dateOfBirth;

    @Column(name = "SecurityQuestion1", length = 255)
    private String securityQuestion1;

    @Column(name = "SecurityAnswer1", length = 255)
    private String securityAnswer1;

    @Column(name = "SecurityQuestion2", length = 255)
    private String securityQuestion2;

    @Column(name = "SecurityAnswer2", length = 255)
    private String securityAnswer2;

    @Column(name = "IsActive", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserPhone> phones;

    // Constructors
    public User() {
        this.phones = new ArrayList<>(); // Initialize to avoid null
    }

    public User(String username, String passwordHash, String firstName, String lastName, String email, Role role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
        this.phones = new ArrayList<>();
    }

    // Helper methods for bidirectional relationship
    public void addPhone(UserPhone phone) {
        if (phones == null) {
            phones = new ArrayList<>();
        }
        phones.add(phone);
        phone.setUser(this); // Ensure bidirectional consistency
    }

    public void removePhone(UserPhone phone) {
        phones.remove(phone);
        phone.setUser(null); // Clear reference
    }
}
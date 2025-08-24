package com.ceylonbank.webbasedbankingsystem.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Roles")
@Data
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RoleID")
    private Integer roleId;

    @Column(name = "RoleName", unique = true, nullable = false, length = 50)
    private String roleName;

    @Column(name = "Description", length = 200)
    private String description;

    // Constructors
    public Role() {}

    public Role(String roleName, String description) {
        this.roleName = roleName;
        this.description = description;
    }
}
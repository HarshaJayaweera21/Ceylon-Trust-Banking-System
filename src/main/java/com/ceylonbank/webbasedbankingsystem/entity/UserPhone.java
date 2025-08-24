package com.ceylonbank.webbasedbankingsystem.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "UserPhones")
@Data
public class UserPhone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PhoneID")
    private Integer phoneId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private User user;

    @Column(name = "PhoneNumber", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "PhoneType", length = 20)
    private String phoneType;
}

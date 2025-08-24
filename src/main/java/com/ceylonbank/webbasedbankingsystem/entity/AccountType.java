package com.ceylonbank.webbasedbankingsystem.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "AccountTypes")
@Data
public class AccountType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer typeId;

    @Column(name = "TypeName", unique = true, nullable = false)
    private String typeName;

    @Column(name = "InterestRate", precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(name = "Description")
    private String description;
}

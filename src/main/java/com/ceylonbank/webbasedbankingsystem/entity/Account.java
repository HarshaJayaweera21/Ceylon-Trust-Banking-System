package com.ceylonbank.webbasedbankingsystem.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "Accounts")
@Data
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AccountID")
    private Integer accountId;

    @Column(name = "UserID")
    private Integer userId;

    @Column(name = "TypeID")
    private Integer typeId;

    @Column(name = "AccountNumber")
    private String accountNumber;

    @Column(name = "Balance")
    private BigDecimal balance;

    @Column(name = "AccruedInterest")
    private BigDecimal accruedInterest;

    @Column(name = "LastAccrualDate")
    private java.time.LocalDate lastAccrualDate;

    @Column(name = "Status")
    private String status;

    @Column(name = "ApprovedBy")
    private Integer approvedBy;

    @Column(name = "OpenedAt")
    private java.time.LocalDateTime openedAt;

    @Column(name = "ClosedAt")
    private java.time.LocalDateTime closedAt;

    @Column(name = "IsActive")
    private Boolean isActive;
    
    // Virtual field for template compatibility
    @Transient
    private String typeName;
    
    // Helper method to get type name
    public String getTypeName() {
        if (typeId != null) {
            return typeId == 1 ? "Savings" : "Current";
        }
        return "Unknown";
    }
    
    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}
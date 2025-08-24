package com.ceylonbank.webbasedbankingsystem.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Transactions")
@Data
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TransactionID")
    private Integer transactionId;
    
    @Column(name = "AccountID")
    private Integer accountId;
    
    @Column(name = "TargetAccountID")
    private Integer targetAccountId;
    
    @Column(name = "Type")
    private String type;
    
    @Column(name = "Amount")
    private BigDecimal amount;
    
    @Column(name = "Description")
    private String description;
    
    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;
    
    @Column(name = "Status")
    private String status;
    
    @Column(name = "ReferenceNumber")
    private String referenceNumber;
    
    @Column(name = "PerformedBy")
    private Integer performedBy;
    
    // Virtual field for template compatibility
    @Transient
    private String accountType;
    
    // Helper method to get account type
    public String getAccountType() {
        if (accountType != null) {
            return accountType;
        }
        return "Unknown";
    }
    
    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
}

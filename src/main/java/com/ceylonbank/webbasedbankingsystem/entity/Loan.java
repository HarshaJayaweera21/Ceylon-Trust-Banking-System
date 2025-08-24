package com.ceylonbank.webbasedbankingsystem.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Loans")
@Data
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LoanID")
    private Integer loanId;

    @Column(name = "UserID")
    private Integer userId;
    
    @Column(name = "LoanType")
    private String loanType;
    
    @Column(name = "Amount")
    private BigDecimal amount;
    
    @Column(name = "InterestRate")
    private BigDecimal interestRate;
    
    @Column(name = "TermMonths")
    private Integer termMonths;
    
    @Column(name = "Status")
    private String status;
    
    @Column(name = "ReviewedBy")
    private Integer reviewedBy;
    
    @Column(name = "ApprovedBy")
    private Integer approvedBy;
    
    @Column(name = "AppliedAt")
    private LocalDateTime appliedAt;
    
    @Column(name = "Comments")
    private String comments;

    @PrePersist
    protected void onCreate() {
        if (appliedAt == null) {
            appliedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "Pending";
        }
    }
}

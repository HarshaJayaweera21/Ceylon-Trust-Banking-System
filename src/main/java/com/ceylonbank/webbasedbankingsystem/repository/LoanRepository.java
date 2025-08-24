package com.ceylonbank.webbasedbankingsystem.repository;

import com.ceylonbank.webbasedbankingsystem.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Integer> {
    List<Loan> findByUserId(Integer userId);
}

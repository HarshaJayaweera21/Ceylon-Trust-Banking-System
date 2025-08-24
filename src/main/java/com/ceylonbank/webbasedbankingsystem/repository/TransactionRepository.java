package com.ceylonbank.webbasedbankingsystem.repository;

import com.ceylonbank.webbasedbankingsystem.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    @Query("SELECT t FROM Transaction t WHERE t.accountId IN (SELECT a.accountId FROM Account a WHERE a.userId = ?1) ORDER BY t.createdAt DESC")
    List<Transaction> findRecentByUserId(Integer userId);
    
    @Query(value = "SELECT TOP (?2) t.* FROM Transactions t " +
                   "INNER JOIN Accounts a ON t.AccountID = a.AccountID " +
                   "WHERE a.UserID = ?1 " +
                   "ORDER BY t.CreatedAt DESC", nativeQuery = true)
    List<Transaction> findRecentTransactionsByUserId(Integer userId, int limit);
}

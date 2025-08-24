package com.ceylonbank.webbasedbankingsystem.repository;


import com.ceylonbank.webbasedbankingsystem.entity.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountTypeRepository extends JpaRepository<AccountType, Integer> {
}

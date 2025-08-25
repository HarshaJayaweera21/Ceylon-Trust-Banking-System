//package com.ceylonbank.webbasedbankingsystem.service;
//
//import com.ceylonbank.webbasedbankingsystem.entity.Account;
//import com.ceylonbank.webbasedbankingsystem.entity.AccountType;
//import com.ceylonbank.webbasedbankingsystem.repository.AccountRepository;
//import com.ceylonbank.webbasedbankingsystem.repository.AccountTypeRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.math.BigDecimal;
//import java.util.List;
//import java.util.Random;
//
//@Service
//public class AccountService {
//
//    @Autowired
//    private AccountRepository accountRepository;
//
//    @Autowired
//    private AccountTypeRepository accountTypeRepository;
//
//    public List<AccountType> getAllAccountTypes() {
//        return accountTypeRepository.findAll();
//    }
//
//    public void createAccount(Integer userId, Integer typeId) {
//        if (accountRepository.existsByUserIdAndTypeId(userId, typeId)) {
//            throw new IllegalArgumentException("You already have an account of this type");
//        }
//
//        AccountType accountType = accountTypeRepository.findById(typeId)
//                .orElseThrow(() -> new IllegalArgumentException("Invalid account type"));
//
//        Account account = new Account();
//        account.setUserId(userId);
//        account.setTypeId(typeId);
//        account.setAccountNumber(generateAccountNumber(accountType.getTypeName()));
//        account.setBalance(BigDecimal.ZERO);
//        account.setStatus("Pending");
//        account.setIsActive(false);
//
//        accountRepository.save(account);
//    }
//
//    private String generateAccountNumber(String typeName) {
//        String prefix = typeName.equals("Savings") ? "SAV-" : "CUR-";
//        Random random = new Random();
//        int randomNum = 10000000 + random.nextInt(90000000); // 8 random digits
//        return prefix + randomNum;
//    }
//}

package com.ceylonbank.webbasedbankingsystem.service;

import com.ceylonbank.webbasedbankingsystem.entity.Account;
import com.ceylonbank.webbasedbankingsystem.entity.AccountType;
import com.ceylonbank.webbasedbankingsystem.entity.User;
import com.ceylonbank.webbasedbankingsystem.repository.AccountRepository;
import com.ceylonbank.webbasedbankingsystem.repository.AccountTypeRepository;
import com.ceylonbank.webbasedbankingsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountTypeRepository accountTypeRepository;

    @Autowired
    private UserRepository userRepository;

    public List<AccountType> getAllAccountTypes() {
        return accountTypeRepository.findAll();
    }

    public void createAccount(Integer userId, Integer typeId, BigDecimal initialDeposit, String nic) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!user.getNic().equals(nic)) {
            throw new IllegalArgumentException("NIC does not match user's record");
        }
        if (accountRepository.existsByUserIdAndTypeId(userId, typeId)) {
            throw new IllegalArgumentException("You already have an account of this type");
        }
        if (initialDeposit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Initial deposit cannot be negative");
        }

        AccountType accountType = accountTypeRepository.findById(typeId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid account type"));

        Account account = new Account();
        account.setUserId(userId);
        account.setTypeId(typeId);
        account.setAccountNumber(generateAccountNumber(accountType.getTypeName()));
        account.setBalance(initialDeposit);
        account.setStatus("Pending");
        account.setIsActive(false);

        accountRepository.save(account);
    }

    private String generateAccountNumber(String typeName) {
        String prefix = typeName.equals("Savings") ? "SAV-" : "CUR-";
        Random random = new Random();
        int randomNum = 10000000 + random.nextInt(90000000); // 8 random digits
        return prefix + randomNum;
    }
    
    public List<Account> getAccountsByUserId(Integer userId) {
        return accountRepository.findByUserId(userId);
    }
    
    public void closeAccount(Integer accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        
        // Check if account is active
        if (!account.getIsActive()) {
            throw new IllegalArgumentException("Account is already inactive");
        }
        
        // Check if account status is approved
        if (!"Approved".equals(account.getStatus())) {
            throw new IllegalArgumentException("Only approved accounts can be closed");
        }
        
        // Check if account has zero balance
        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalArgumentException("Account must have zero balance to be closed");
        }
        
        // Close the account
        account.setIsActive(false);
        account.setStatus("Closed");
        account.setClosedAt(LocalDateTime.now());
        
        accountRepository.save(account);
    }
    
    public void openAccount(Integer accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        
        // Check if account is inactive
        if (account.getIsActive()) {
            throw new IllegalArgumentException("Account is already active");
        }
        
        // Check if account status is closed
        if (!"Closed".equals(account.getStatus())) {
            throw new IllegalArgumentException("Only closed accounts can be reopened");
        }
        
        // Reopen the account
        account.setIsActive(true);
        account.setStatus("Approved");
        account.setClosedAt(null);
        
        accountRepository.save(account);
    }
}

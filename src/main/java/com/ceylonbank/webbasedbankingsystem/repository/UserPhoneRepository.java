package com.ceylonbank.webbasedbankingsystem.repository;

import com.ceylonbank.webbasedbankingsystem.entity.UserPhone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPhoneRepository extends JpaRepository<UserPhone, Integer> {
    List<UserPhone> findByUserUserId(Integer userId);
    void deleteByUserUserId(Integer userId);
}
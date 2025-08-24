//package com.ceylonbank.webbasedbankingsystem.repository;
//
//import com.ceylonbank.webbasedbankingsystem.entity.User;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public interface UserRepository extends JpaRepository<User, Integer> {
//    Optional<User> findByUsername(String username);
//    Optional<User> findByEmail(String email);
//    List<User> findByRole_RoleName(String roleName);
//    boolean existsByUsername(String username);
//    boolean existsByEmail(String email);
//}


package com.ceylonbank.webbasedbankingsystem.repository;

import com.ceylonbank.webbasedbankingsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByNic(String nic);
    List<User> findByNicStartingWithIgnoreCase(String nic);
    List<User> findByRole_RoleName(String roleName);
    List<User> findByRole_RoleNameNot(String roleName);
    List<User> findByFirstNameContainingIgnoreCase(String firstName);
    List<User> findByLastNameContainingIgnoreCase(String lastName);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByNic(String nic);
}
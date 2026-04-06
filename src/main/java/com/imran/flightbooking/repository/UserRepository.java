package com.imran.flightbooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.imran.flightbooking.entity.User;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
    
    /**
     * Get all users (can be extended with custom queries for complex searches)
     */
    @Query("SELECT u FROM User u ORDER BY u.userId")
    List<User> findAllUsers();
    
    /**
     * Find users by status
     */
    List<User> findByStatus(String status);
}
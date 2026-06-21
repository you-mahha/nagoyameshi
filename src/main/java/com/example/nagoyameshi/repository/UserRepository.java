package com.example.nagoyameshi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nagoyameshi.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

	List<User> findByEmailContaining(String email);

    User findByEmail(String email);
    boolean existsByEmail(String email);
    User findByStripeCustomerId(String stripeCustomerId);
    User findByStripeSubscriptionId(String stripeSubscriptionId);
}


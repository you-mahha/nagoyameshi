package com.example.nagoyameshi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nagoyameshi.entity.Favorite;
import com.example.nagoyameshi.entity.Shop;
import com.example.nagoyameshi.entity.User;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByUserAndShop(User user, Shop shop);

    Optional<Favorite> findByUserAndShop(User user, Shop shop);

    List<Favorite> findByUserOrderByCreatedAtDesc(User user);
}
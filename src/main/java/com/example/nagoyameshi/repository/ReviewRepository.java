package com.example.nagoyameshi.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.nagoyameshi.entity.Review;
import com.example.nagoyameshi.entity.Shop;
import com.example.nagoyameshi.entity.User;

public interface ReviewRepository extends JpaRepository<Review, Long> {
	
	// 平均点
    @Query("SELECT AVG(r.score) FROM Review r WHERE r.shop.id = :shopId")
    Double findAverageScoreByShopId(@Param("shopId") Long shopId);

    // 件数
    long countByShopId(Long shopId);
    Page<Review> findByShopOrderByCreatedAtDesc(
            Shop shop,
            Pageable pageable);

    Page<Review> findByShopOrderByScoreDesc(
            Shop shop,
            Pageable pageable);

    List<Review> findByUserOrderByCreatedAtDesc(User user);
    
    boolean existsByUserIdAndShopId(Long userId, Long shopId);
}

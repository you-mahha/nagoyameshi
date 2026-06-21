package com.example.nagoyameshi.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.nagoyameshi.entity.Shop;

public interface ShopRepository extends JpaRepository<Shop, Long> {
	 Page<Shop> findByNameContaining(String keyword, Pageable pageable);
	 Page<Shop> findByCategoryId(Long categoryId, Pageable pageable);
	 Page<Shop> findByNameContainingAndCategoryId(String keyword, Long categoryId, Pageable pageable);
	 
	 List<Shop> findByNameContaining(String keyword);

	 List<Shop> findByCategoryId(Long categoryId);

	 List<Shop> findByNameContainingAndCategoryId(String keyword, Long categoryId);
	 
	 boolean existsByCategoryId(Long categoryId);
	 @Query("""
			 SELECT s FROM Shop s
			 LEFT JOIN Review r ON r.shop.id = s.id
			 GROUP BY s
			 ORDER BY AVG(r.score) DESC
			 """)
			 Page<Shop> findAllOrderByAverageScoreDesc(Pageable pageable);
}

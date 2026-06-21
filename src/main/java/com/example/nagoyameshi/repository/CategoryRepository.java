package com.example.nagoyameshi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nagoyameshi.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
	
	 // 同じ名前のカテゴリが存在するか？
    boolean existsByName(String name);
} // interfaceはこの注文書を渡せば、商品が届くという意味。Repositoryはinterfacenのみ

package com.example.nagoyameshi.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.nagoyameshi.entity.Review;
import com.example.nagoyameshi.entity.Shop;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.repository.CategoryRepository;
import com.example.nagoyameshi.repository.FavoriteRepository;
import com.example.nagoyameshi.repository.ReviewRepository;
import com.example.nagoyameshi.repository.ShopRepository;
import com.example.nagoyameshi.repository.UserRepository;

@RequestMapping("/shops")
@Controller
public class ShopController {

	private final ShopRepository shopRepository;
	private final ReviewRepository reviewRepository;
	private final UserRepository userRepository;
	private final CategoryRepository categoryRepository;
	private final FavoriteRepository favoriteRepository;
	
	public ShopController(
			ShopRepository shopRepository,
			ReviewRepository reviewRepository,
			UserRepository userRepository,
			CategoryRepository categoryRepository,
			FavoriteRepository favoriteRepository) {
		this.shopRepository = shopRepository;
		this.reviewRepository = reviewRepository;
		this.userRepository = userRepository;
		this.categoryRepository = categoryRepository;
		this.favoriteRepository = favoriteRepository;
	}

	// 一覧（※ 今回は使っていなくてもOK）
	@GetMapping
	public String list(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(required = false) String keyword,
			@RequestParam(required = false) Long categoryId, 
			@RequestParam(required = false) String sort, // 検索機能
			Model model) {

		Pageable pageable = PageRequest.of(page, 5); // 5件ごと
		
		 if ("name".equals(sort)) {
		        pageable = PageRequest.of(page, 5, Sort.by("name").ascending());
		    } else if ("category".equals(sort)) {
		        pageable = PageRequest.of(page, 5, Sort.by("category.name").ascending());
		    } else {
		        pageable = PageRequest.of(page, 5, Sort.by("id").descending());
		    }
		    
		Page<Shop> shopPage;

		boolean hasKeyword = keyword != null && !keyword.isBlank();
		boolean hasCategory = categoryId != null;

		if ("rating".equals(sort)) {
		    shopPage = shopRepository.findAllOrderByAverageScoreDesc(pageable);
		} else if (hasKeyword && hasCategory) {
	        shopPage = shopRepository.findByNameContainingAndCategoryId(keyword, categoryId, pageable);
	    } else if (hasKeyword) {
	        shopPage = shopRepository.findByNameContaining(keyword, pageable);
	    } else if (hasCategory) {
	        shopPage = shopRepository.findByCategoryId(categoryId, pageable);
	    } else {
	        shopPage = shopRepository.findAll(pageable);
		}
	    
	   
	    model.addAttribute("shopPage", shopPage);
	    model.addAttribute("shops", shopPage.getContent());
	    model.addAttribute("keyword", keyword);
	    model.addAttribute("categoryId", categoryId);
	    model.addAttribute("categories", categoryRepository.findAll());
	    model.addAttribute("sort", sort);
	    
		return "shops/list";
	}

	// 店舗詳細
	@GetMapping("/{id}")
	public String detail(
			@PathVariable Long id,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(required = false) String sort,
			Model model,
			@AuthenticationPrincipal UserDetails userDetails) {

		Shop shop = shopRepository.findById(id).orElseThrow();
		model.addAttribute("shop", shop);

		// レビュー一覧
		// 並び替え分岐
		Pageable pageable = PageRequest.of(page, 5);

		Page<Review> reviewPage;

		if ("score".equals(sort)) {
			reviewPage = reviewRepository.findByShopOrderByScoreDesc(shop, pageable);
		} else {
			reviewPage = reviewRepository.findByShopOrderByCreatedAtDesc(shop, pageable);
		}

		model.addAttribute("reviewPage", reviewPage);
		model.addAttribute("reviews", reviewPage.getContent());

		// Controller追記(平均評価)
		Double averageScore = reviewRepository.findAverageScoreByShopId(shop.getId());
		long reviewCount = reviewRepository.countByShopId(shop.getId());

		// レビューが0件のとき対策
		if (averageScore == null) {
			averageScore = 0.0;
		} else {
			averageScore = Math.round(averageScore * 10.0) / 10.0;
		}
		model.addAttribute("averageScore", averageScore);
		model.addAttribute("reviewCount", reviewCount);

		// ログインユーザー（いれば）
		if (userDetails != null) {
			User user = userRepository
					.findByEmail(userDetails.getUsername());
			model.addAttribute("user", user);
			
			boolean isFavorite = favoriteRepository.existsByUserAndShop(user, shop);
			model.addAttribute("isFavorite", isFavorite);
		}

		return "shops/detail";
	}
}

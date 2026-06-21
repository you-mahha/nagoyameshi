package com.example.nagoyameshi.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.Shop;
import com.example.nagoyameshi.repository.CategoryRepository;
import com.example.nagoyameshi.repository.ShopRepository;
import com.example.nagoyameshi.repository.UserRepository;

@Controller
@RequestMapping("/admin")
public class AdminController {

	private final UserRepository userRepository;
	private final ShopRepository shopRepository;
	private final CategoryRepository categoryRepository;

	public AdminController(UserRepository userRepository,
			ShopRepository shopRepository, CategoryRepository categoryRepository) {
		this.userRepository = userRepository;
		this.shopRepository = shopRepository;
		this.categoryRepository = categoryRepository;
	}

	@GetMapping // 追加
	public String adminTop() {
		return "redirect:/admin/dashboard";
	}

	@GetMapping("/dashboard")
	public String dashboard() {
		return "admin/dashboard";
	}

	@PostMapping("/shops/create")
	public String create(
			@ModelAttribute Shop shop,
			@RequestParam("imageFile") MultipartFile imageFile,
			@RequestParam("categoryId") Long categoryId) throws Exception {

		 // カテゴリ設定
	    Category category = categoryRepository
	            .findById(categoryId)
	            .orElse(null);
	    shop.setCategory(category);
		
		// ★ 画像がある場合だけ処理
		if (!imageFile.isEmpty()) {

			// ファイル名をユニークにする
			String fileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();

			// 保存先
			Path filePath = Paths.get("uploads/images/" + fileName);

			// 保存
			Files.copy(imageFile.getInputStream(), filePath);

			// DBに保存
			shop.setImageUrl("/images/" + fileName);
		}

		System.out.println("ファイル名: " + imageFile.getOriginalFilename());
		System.out.println("空かどうか: " + imageFile.isEmpty());
		shopRepository.save(shop);

		return "redirect:/shops";
	}
}

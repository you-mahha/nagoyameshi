package com.example.nagoyameshi.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.Shop;
import com.example.nagoyameshi.repository.CategoryRepository;
import com.example.nagoyameshi.repository.ShopRepository;

@Controller
@RequestMapping("/admin/shops")
public class AdminShopController {

    private final ShopRepository shopRepository;
    private final CategoryRepository categoryRepository;

    public AdminShopController(ShopRepository shopRepository,
                               CategoryRepository categoryRepository) {
        this.shopRepository = shopRepository;
        this.categoryRepository = categoryRepository;
    }

    // 店舗一覧
    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
    		@RequestParam(required = false) Long categoryId,
    		Model model) {
    	List<Shop> shops;

        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasCategory = categoryId != null;

        if (hasKeyword && hasCategory) {
            shops = shopRepository.findByNameContainingAndCategoryId(keyword, categoryId);
        } else if (hasKeyword) {
            shops = shopRepository.findByNameContaining(keyword);
        } else if (hasCategory) {
            shops = shopRepository.findByCategoryId(categoryId);
        } else {
            shops = shopRepository.findAll();
        }

        model.addAttribute("shops", shops);
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("categories", categoryRepository.findAll());

        return "admin/shops/list";
    }

    // 新規作成画面
    @GetMapping("/new")
    public String newShop(Model model) {
        model.addAttribute("shop", new Shop()); // 必須
        model.addAttribute("categories", categoryRepository.findAll()); // 必須
        return "admin/shops/new";
    }

    // 新規作成処理
    @PostMapping("/new")
    public String create(@ModelAttribute Shop shop,
                         @RequestParam(name = "categoryId", required = false) String categoryIdStr) {

    	if (categoryIdStr != null && !categoryIdStr.isEmpty()) {
            try {
                Long categoryId = Long.valueOf(categoryIdStr);
                Category category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid category id"));
                shop.setCategory(category);
            } catch (NumberFormatException e) {
                shop.setCategory(null); // 無効な値の場合は null
            }
        } else {
            shop.setCategory(null); // 未設定の場合は null
        }

        shopRepository.save(shop);
        return "redirect:/admin/shops";
    }



    // 編集画面
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("店舗が存在しません"));
        model.addAttribute("shop", shop);
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/shops/edit";
    }

    // 更新処理
    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @RequestParam String name,
                         @RequestParam String address,
                         @RequestParam(name = "categoryId", required = false) String categoryIdStr,
                         @RequestParam(name = "imageFile", required = false) MultipartFile imageFile) throws IOException { // 画像送る処理追加 //


        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid shop id"));

        shop.setName(name);
        shop.setAddress(address);

        if (categoryIdStr != null && !categoryIdStr.isEmpty()) {
            try {
                Long categoryId = Long.valueOf(categoryIdStr);
                Category category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid category id"));
                shop.setCategory(category);
            } catch (NumberFormatException e) {
                shop.setCategory(null);
            }
        } else {
            shop.setCategory(null);
        }

     // 画像が選択されている場合だけ更新
        if (imageFile != null && !imageFile.isEmpty()) {
        	String originalFileName = imageFile.getOriginalFilename();
        	String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        	String fileName = UUID.randomUUID().toString() + extension;

            Path uploadPath = Paths.get("uploads/images");

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(imageFile.getInputStream(), filePath);

            shop.setImageUrl("/images/" + fileName);
        }
        
        shopRepository.save(shop);
        return "redirect:/admin/shops";
    }

    // 削除
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        shopRepository.deleteById(id);
        return "redirect:/admin/shops";
    }
}

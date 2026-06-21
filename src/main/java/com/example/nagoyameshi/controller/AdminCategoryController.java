package com.example.nagoyameshi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.repository.CategoryRepository;
import com.example.nagoyameshi.repository.ShopRepository;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final CategoryRepository categoryRepository;
    private final ShopRepository shopRepository;

    public AdminCategoryController(CategoryRepository categoryRepository,
    		ShopRepository shopRepository) {
        this.categoryRepository = categoryRepository;
        this.shopRepository = shopRepository;
    }

    // 一覧表示
    @GetMapping
    public String index(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/categories/index";
    }
    
    // 新規作成画面
    @GetMapping("/new")
    public String newCategory(Model model) {
        model.addAttribute("category", new Category());
        return "admin/categories/new";
    }

    // 登録
    @PostMapping
    public String create(@ModelAttribute Category category,
    		RedirectAttributes redirectAttributes) {
    	categoryRepository.save(category);
    	
    	redirectAttributes.addFlashAttribute(
    			"successMessage", "カテゴリを登録しました。");
    	return "redirect:/admin/categories";
    	}
    
    // 編集画面
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        Category category = categoryRepository.findById(id).orElseThrow();
        model.addAttribute("category", category);
        return "admin/categories/edit";
    }
    
    // 更新
    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
    		@ModelAttribute Category formCategory,
    		RedirectAttributes redirectAttributes) {
    	
    	Category category  = categoryRepository.findById(id).orElseThrow();
    	category.setName(formCategory.getName());
    	
    	categoryRepository.save(category);
    	redirectAttributes.addFlashAttribute(
    			"successMessage", "カテゴリを更新しました。");
    	
    	return "redirect:/admin/categories";
    }
    
    // 削除
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
    		RedirectAttributes redirectAttributes) {
    	if (shopRepository.existsByCategoryId(id)) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "このカテゴリは店舗に使用されているため削除できません");

            return "redirect:/admin/categories";
        }
    	
    	categoryRepository.deleteById(id);
    	
    	redirectAttributes.addFlashAttribute(
    			"successMessage", "カテゴリを削除しました。");
    	return "redirect:/admin/categories";
    }
}

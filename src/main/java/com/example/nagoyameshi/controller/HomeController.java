package com.example.nagoyameshi.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.repository.ShopRepository;
import com.example.nagoyameshi.repository.UserRepository;

@Controller // 画面表示のためのアノテーション
public class HomeController {

	 private final ShopRepository shopRepository;
	 private final UserRepository userRepository;

	 public HomeController(ShopRepository shopRepository,
             UserRepository userRepository) {
this.shopRepository = shopRepository;
this.userRepository = userRepository;
}

	    
	    // 一般ユーザー用トップページ
	 @GetMapping("/")
	    public String index(Model model) {

	        // 店舗一覧
		 model.addAttribute("shops", shopRepository.findAll());

		    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	        

	        // ログイン中ユーザーがいれば渡す
	        if (auth != null) {
	            User user = userRepository
	            		 .findByEmail(auth.getName());
	            model.addAttribute("user", user);
	        }

	        return "home/index";
	    }
	}

package com.example.nagoyameshi.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.repository.UserRepository;

@Controller
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 有料会員アップグレード画面
    @GetMapping("/user/upgrade")
    public String upgradeForm() {
        return "user/upgrade";
    }

    // 有料会員にアップグレード（例: 1ヶ月サブスク）
    @PostMapping("/user/upgrade")
    public String upgradeToPremium(@AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository
            .findByEmail(userDetails.getUsername());

        user.setPremium(true);
        userRepository.save(user);

        return "redirect:/";
    }
}

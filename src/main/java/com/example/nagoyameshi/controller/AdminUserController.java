package com.example.nagoyameshi.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.repository.UserRepository;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserRepository userRepository;

    public AdminUserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @PostMapping("/toggle/{id}")
    public String toggleUserEnabled(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails loginUser
    ) {
        User targetUser = userRepository.findById(id)
                .orElseThrow();

        // ★ 自分自身を無効化しようとしたら拒否
        if (targetUser.getEmail().equals(loginUser.getUsername())) {
            throw new IllegalStateException("管理者自身は無効化できません");
        }

        targetUser.setEnabled(!targetUser.isEnabled());
        userRepository.save(targetUser);

        return "redirect:/admin/users";
        
        
    }
    
    
    @PostMapping("/upgrade/{id}")
    public String upgradeUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails loginUser
    ) {
        User targetUser = userRepository.findById(id)
                .orElseThrow();

        // ★ 一般ユーザーのみアップグレード可能
        if (!targetUser.getRole().equals("ROLE_USER")) {
            throw new IllegalStateException("一般ユーザーのみアップグレード可能です");
        }

        // すでに有料なら何もしない（保険）
        if (targetUser.isPremium()) {
            return "redirect:/admin/users";
        }

        // 有料会員にする（例：30日）
        targetUser.setPremium(true);
        targetUser.setPremiumValidUntil(
                java.time.LocalDate.now().plusDays(30)
        );

        userRepository.save(targetUser);

        return "redirect:/admin/users";
    }

    @GetMapping
    public String index(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "membership", required = false) String membership,
            Model model) {

        List<User> users;

        if (keyword != null && !keyword.isBlank()) {
            users = userRepository.findByEmailContaining(keyword);
        } else {
            users = userRepository.findAll();
        }

        if ("premium".equals(membership)) {
            users = users.stream()
                    .filter(User::isPremium)
                    .toList();
        } else if ("free".equals(membership)) {
            users = users.stream()
                    .filter(user -> !user.isPremium())
                    .toList();
        }

        model.addAttribute("users", users);
        model.addAttribute("keyword", keyword);
        model.addAttribute("membership", membership);

        return "admin/users/list";
    }
}

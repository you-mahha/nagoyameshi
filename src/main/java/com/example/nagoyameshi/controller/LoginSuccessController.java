package com.example.nagoyameshi.controller;

import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginSuccessController {

    @GetMapping("/login/success")
    public String loginSuccess(Authentication authentication) {

        Set<String> roles =
                AuthorityUtils.authorityListToSet(authentication.getAuthorities());

        // 管理者
        if (roles.contains("ROLE_ADMIN")) {
            return "redirect:/admin/dashboard";
        }

        // 一般ユーザー
        return "redirect:/";
    }
}

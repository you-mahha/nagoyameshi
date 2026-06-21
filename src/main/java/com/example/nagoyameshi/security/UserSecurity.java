package com.example.nagoyameshi.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.example.nagoyameshi.entity.User;

@Component("userSecurity")
public class UserSecurity {

    // Spring Security の認証情報からプレミアムかどうかを判定
    public boolean isPremium(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            // ここで DB の User エンティティを取得して premium をチェックする
            // 例: ユーザー名（email）で取得
            User user = loadUserByEmail(userDetails.getUsername());
            return user != null && user.isPremium();
        }

        return false;
    }

    // 仮メソッド：UserRepository を @Autowired して DB から取得する
    private User loadUserByEmail(String email) {
        // ここに UserRepository を使って DB から取得
        // return userRepository.findByEmail(email).orElse(null);
        return null; // 仮置き
    }
}

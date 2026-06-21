package com.example.nagoyameshi.controller;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.nagoyameshi.entity.PasswordResetToken;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.repository.PasswordResetTokenRepository;
import com.example.nagoyameshi.repository.UserRepository;

@Controller
@RequestMapping("/password-reset")
public class PasswordResetController {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetController(UserRepository userRepository,
                                   PasswordResetTokenRepository tokenRepository,
                                   PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String requestForm() {
        return "password-reset/request";
    }

    @PostMapping
    public String sendResetLink(@RequestParam String email, Model model) {

        User user = userRepository.findByEmail(email);

        if (user == null) {
            model.addAttribute("message", "入力されたメールアドレス宛に再設定リンクを送信しました。");
            return "password-reset/request";
        }

        tokenRepository.deleteByUserId(user.getId());

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setToken(token);
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(30));

        tokenRepository.save(resetToken);

        model.addAttribute("resetUrl", "http://localhost:8080/password-reset/edit?token=" + token);

        return "password-reset/sent";
    }

    @GetMapping("/edit")
    public String editForm(@RequestParam String token, Model model) {

        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElse(null);

        if (resetToken == null || resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            model.addAttribute("error", "再設定リンクが無効、または期限切れです。");
            return "password-reset/request";
        }

        model.addAttribute("token", token);
        return "password-reset/edit";
    }

    @PostMapping("/update")
    public String updatePassword(@RequestParam String token,
                                 @RequestParam String password,
                                 Model model) {

        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElse(null);

        if (resetToken == null || resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            model.addAttribute("error", "再設定リンクが無効、または期限切れです。");
            return "password-reset/request";
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);

        tokenRepository.delete(resetToken);

        return "redirect:/login?passwordUpdated";
    }
}
package com.example.nagoyameshi.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.nagoyameshi.entity.Favorite;
import com.example.nagoyameshi.entity.Review;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.UserEditForm;
import com.example.nagoyameshi.repository.FavoriteRepository;
import com.example.nagoyameshi.repository.ReviewRepository;
import com.example.nagoyameshi.repository.UserRepository;
import com.stripe.Stripe;
import com.stripe.model.Subscription;
import com.stripe.param.SubscriptionUpdateParams;

import jakarta.validation.Valid;

@Controller
public class MyPageController {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final FavoriteRepository favoriteRepository;

    @Value("${stripe.secret-key}")
    private String stripeApiKey;

    public MyPageController(UserRepository userRepository,
                            ReviewRepository reviewRepository,
                            FavoriteRepository favoriteRepository) {
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.favoriteRepository = favoriteRepository;
    }

    @GetMapping("/mypage")
    public String mypage(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {

        User user = userRepository.findByEmail(userDetails.getUsername());

        if (user == null) {
            return "redirect:/login";
        }
        
        List<Review> reviews =
                reviewRepository.findByUserOrderByCreatedAtDesc(user);
        

        model.addAttribute("isPremium", user.isPremium());
        model.addAttribute("reviews", reviews);
        model.addAttribute("user", user);
        
        List<Favorite> favorites =
        		favoriteRepository.findByUserOrderByCreatedAtDesc(user);
        
        model.addAttribute("favorites", favorites);

        return "mypage/index";
    }
    
    @GetMapping("/mypage/edit") // 編集機能
    public String editForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {

    	User user = userRepository.findByEmail(userDetails.getUsername());
    	
    	UserEditForm userEditForm = new UserEditForm();
    	userEditForm.setName(user.getName());
    	userEditForm.setEmail(user.getEmail());
    	
    	model.addAttribute("userEditForm", userEditForm);
    	
    	return "mypage/edit";
    }
    
    @PostMapping("/mypage/edit")
    public String updateUser(@AuthenticationPrincipal UserDetails userDetails,
    		@ModelAttribute @Valid UserEditForm userEditForm,
    		BindingResult bindingResult, Model model) {
    	
    	if(bindingResult.hasErrors()) {
    		return "mypage/edit";
    	}
    	
    	User user = userRepository.findByEmail(userDetails.getUsername());
    	
    	if (user == null) {
            return "redirect:/login";
        }
    	
    	// 重複アドレスチェック
    	User existingUser = userRepository.findByEmail(userEditForm.getEmail());
        if (existingUser != null && !existingUser.getId().equals(user.getId())) {
            model.addAttribute("errorMessage", "そのメールアドレスは既に使われています。");
            return "mypage/edit";
        }

        boolean emailChanged = !userDetails.getUsername().equals(userEditForm.getEmail());
        
    	user.setName(userEditForm.getName());
    	user.setEmail(userEditForm.getEmail());
    	userRepository.save(user);
    	
    	if (emailChanged) {
            return "redirect:/login";
        }
        
    	return "redirect:/mypage?profileUpdated";
        }
    
    @PostMapping("/cancel")
    public String cancelPremium(@AuthenticationPrincipal UserDetails userDetails) throws Exception {

        Stripe.apiKey = stripeApiKey;

        User user = userRepository.findByEmail(userDetails.getUsername());

        // subscriptionId がないなら何もしない
        if (user.getStripeSubscriptionId() == null || user.getStripeSubscriptionId().isBlank()) {
            System.out.println("❌ subscriptionIdがありません");
            return "redirect:/mypage";
        }

        // Stripe上のサブスク取得
        Subscription subscription = Subscription.retrieve(user.getStripeSubscriptionId());

        // 期間終了時に解約
        SubscriptionUpdateParams params =
                SubscriptionUpdateParams.builder()
                        .setCancelAtPeriodEnd(true)
                        .build();

        subscription.update(params);

        // DBにも解約予定を反映
        user.setCancelAtPeriodEnd(true);
        userRepository.save(user);

        System.out.println("✅ 解約予定に変更しました");

        return "redirect:/mypage?premiumCancel";
    }
}
package com.example.nagoyameshi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.repository.UserRepository;
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;


@Controller
@RequestMapping("/payment")
public class PaymentController {

	
	@Autowired
    private UserRepository userRepository;
	
    @Value("${stripe.secret-key}")
    private String stripeApiKey;
    
    @PostMapping("/checkout")
    public String checkout(@AuthenticationPrincipal UserDetails userDetails) throws Exception {

        Stripe.apiKey = stripeApiKey;

     // ★ログインユーザーをDBから取得
        User user = userRepository.findByEmail(userDetails.getUsername());
        
        SessionCreateParams params =
            SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setSuccessUrl("http://localhost:8080/payment/success")
                .setCancelUrl("http://localhost:8080/payment/cancel")
                .setClientReferenceId(user.getId().toString()) // 追加 Webhookで使用
                .addExpand("subscription")
                .addLineItem(
                    SessionCreateParams.LineItem.builder()
                        .setPrice("price_1TGCTFHiN8soUDyl9hEpBvMF") // ←Stripeで作る(DB二は不要)
                        .setQuantity(1L)
                        .build()
                )
                .build();

        Session session = Session.create(params);

        return "redirect:" + session.getUrl();
    }
    
    // 決済画面に移動
    @GetMapping("/success")
    public String success() {
    	return "redirect:/mypage?premiumSuccess";
    } 
    
    @PostMapping("/portal")
    public String portal(@AuthenticationPrincipal UserDetails userDetails) throws Exception {

        Stripe.apiKey = stripeApiKey;

        User user = userRepository.findByEmail(userDetails.getUsername());

        if (user == null || user.getStripeCustomerId() == null || user.getStripeCustomerId().isBlank()) {
            return "redirect:/mypage?portalError";
        }

        com.stripe.param.billingportal.SessionCreateParams params =
                com.stripe.param.billingportal.SessionCreateParams.builder()
                        .setCustomer(user.getStripeCustomerId())
                        .setReturnUrl("http://localhost:8080/mypage")
                        .build();

        com.stripe.model.billingportal.Session portalSession =
                com.stripe.model.billingportal.Session.create(params);

        return "redirect:" + portalSession.getUrl();
    }
   
    }

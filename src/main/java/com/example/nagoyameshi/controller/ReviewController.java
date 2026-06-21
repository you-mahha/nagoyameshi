package com.example.nagoyameshi.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.nagoyameshi.entity.Review;
import com.example.nagoyameshi.entity.Shop;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.ReviewForm;
import com.example.nagoyameshi.repository.ReviewRepository;
import com.example.nagoyameshi.repository.ShopRepository;
import com.example.nagoyameshi.repository.UserRepository;

@Controller
@RequestMapping("/reviews")
public class ReviewController {

	private final ReviewRepository reviewRepository;
	private final ShopRepository shopRepository;
	private final UserRepository userRepository;

	public ReviewController(ReviewRepository reviewRepository,
			ShopRepository shopRepository,
			UserRepository userRepository) {
		this.reviewRepository = reviewRepository;
		this.shopRepository = shopRepository;
		this.userRepository = userRepository;
	}

	// レビュー投稿画面
	@GetMapping("/new/{shopId}")
	public String newReview(@PathVariable Long shopId,
			Model model) {

		model.addAttribute("shopId", shopId);
		model.addAttribute("reviewForm", new ReviewForm());

		return "reviews/new";
	}

	// 保存処理
	@PostMapping("/{shopId}")
	public String create(@PathVariable Long shopId,
			ReviewForm reviewForm,
			@AuthenticationPrincipal UserDetails userDetails) {

		Shop shop = shopRepository.findById(shopId).orElseThrow();

		User user = userRepository
				.findByEmail(userDetails.getUsername());
		
		 // ★ ここ追加（超重要）
	    if (!user.isPremium()) {
	        return "redirect:/mypage"; // またはエラーメッセージ画面
	    }

	    if (reviewRepository.existsByUserIdAndShopId(user.getId(), shopId)) {
	    	return "redirect:/shops/" + shopId + "?reviewAlreadyExists";
	    }
		Review review = new Review();
	    review.setShop(shop);
	    review.setUser(user);
	    review.setScore(reviewForm.getScore());      // ← ★ 重要
	    review.setComment(reviewForm.getComment());  // ← ★ 重要
		reviewRepository.save(review);

		return "redirect:/shops/" + shopId + "?reviewSuccess";
	}
	
	// 削除処理
	@PostMapping("/delete/{id}")
	public String deleteReview(@PathVariable Long id,
			@AuthenticationPrincipal UserDetails userDetails) {

	    Review review = reviewRepository.findById(id).orElseThrow();
	    User loginUser = userRepository
	            .findByEmail(userDetails.getUsername());

	    // ★ 他人のレビューなら削除させない
	    if (!review.getUser().getId().equals(loginUser.getId())) {
	        return "redirect:/shops/" + review.getShop().getId();
	    }

	    Long shopId = review.getShop().getId();
	    reviewRepository.delete(review);
	    
	    return "redirect:/mypage?reviewDeleted";
	}


}

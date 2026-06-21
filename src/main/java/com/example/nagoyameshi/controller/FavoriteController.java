package com.example.nagoyameshi.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.nagoyameshi.entity.Favorite;
import com.example.nagoyameshi.entity.Shop;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.repository.FavoriteRepository;
import com.example.nagoyameshi.repository.ShopRepository;
import com.example.nagoyameshi.repository.UserRepository;

@Controller
public class FavoriteController {

    private final FavoriteRepository favoriteRepository;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;

    public FavoriteController(FavoriteRepository favoriteRepository,
                              ShopRepository shopRepository,
                              UserRepository userRepository) {
        this.favoriteRepository = favoriteRepository;
        this.shopRepository = shopRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/favorites/{shopId}")
    public String addFavorite(@PathVariable Long shopId,
                              @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername());

        if (user == null || !user.isPremium()) {
            return "redirect:/shops/" + shopId;
        }

        Shop shop = shopRepository.findById(shopId).orElseThrow();

        if (!favoriteRepository.existsByUserAndShop(user, shop)) {
            Favorite favorite = new Favorite();
            favorite.setUser(user);
            favorite.setShop(shop);
            favoriteRepository.save(favorite);
        }

        return "redirect:/shops/" + shopId + "?favoriteAdded";
        
    }

    @PostMapping("/favorites/{shopId}/delete")
    public String deleteFavorite(@PathVariable Long shopId,
                                 @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername());

        if (user == null) {
            return "redirect:/shops/" + shopId;
        }

        Shop shop = shopRepository.findById(shopId).orElseThrow();

        favoriteRepository.findByUserAndShop(user, shop)
                .ifPresent(favoriteRepository::delete);

        return "redirect:/shops/" + shopId + "?favoriteDeleted";
    }
    @PostMapping("/favorites/{shopId}/delete/mypage")
    public String deleteFavoriteFromMypage(@PathVariable Long shopId,
                                           @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername());

        if (user == null) {
            return "redirect:/login";
        }

        Shop shop = shopRepository.findById(shopId).orElseThrow();

        favoriteRepository.findByUserAndShop(user, shop)
                .ifPresent(favoriteRepository::delete);

        return "redirect:/mypage";
    }
}
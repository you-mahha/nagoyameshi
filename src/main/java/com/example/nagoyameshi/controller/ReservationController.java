package com.example.nagoyameshi.controller;

import java.time.LocalDateTime;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.Reservation;
import com.example.nagoyameshi.entity.Shop;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.repository.ReservationRepository;
import com.example.nagoyameshi.repository.ShopRepository;
import com.example.nagoyameshi.repository.UserRepository;

@RequestMapping("/reservations")
@Controller
public class ReservationController {
	
	private final ReservationRepository reservationRepository;
	private final UserRepository userRepository;
	private final ShopRepository shopRepository;
	
	public ReservationController(ReservationRepository reservationRepository, UserRepository userRepository, ShopRepository shopRepository) {
		this.reservationRepository = reservationRepository;
		this.userRepository = userRepository;
		this.shopRepository = shopRepository;
	}

	@GetMapping
    public String index(@AuthenticationPrincipal UserDetails userDetails, Model model) {

        User user = userRepository.findByEmail(userDetails.getUsername());

        if (user == null) {
            return "redirect:/login";
        }

        List<Reservation> reservations =
                reservationRepository.findByUserOrderByCreatedAtDesc(user);

        model.addAttribute("reservations", reservations);

        return "reservations/index";
    }

    // 予約作成画面（仮）
    @GetMapping("/new/{shopId}")
    public String newReservation(@PathVariable Long shopId, Model model) {
        model.addAttribute("shopId", shopId);
        return "reservations/new";
    }
    
    @PostMapping("/{shopId}")
    public String create(@PathVariable Long shopId,
    					@RequestParam Integer numPeople,
    					@RequestParam String reservationDate,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) { // redirectAttribute追加
    

        User user = userRepository.findByEmail(userDetails.getUsername());

        if (user == null) {
            return "redirect:/login";
        }

        Shop shop = shopRepository.findById(shopId).orElseThrow();
        
        LocalDateTime dateTime = LocalDateTime.parse(reservationDate);
        
        // 人数制限(10人まで)
        if (numPeople < 1 || numPeople > 10) {
        	redirectAttributes.addFlashAttribute("error", "人数は1人以上10人以下で入力してください。");
            return "redirect:/reservations/new/" + shopId;
        }
        // 過去の日付での予約不可能
        if(dateTime.isBefore(LocalDateTime.now())) {
        	redirectAttributes.addFlashAttribute("error", "過去の日時は選択できません");	
        	return "redirect:/reservations/new/" + shopId;
        }

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setShop(shop);
        reservation.setReservedDate(dateTime.toLocalDate()); // 仮
        reservation.setReservedTime(dateTime.toLocalTime().toString()); // 仮
        reservation.setNumberOfPeople(numPeople); // 仮

        reservationRepository.save(reservation);

        return "redirect:/reservations";
    }
    
    @PostMapping("/delete/{id}")
    public String deleteReservation(@PathVariable Long id,
                                    @AuthenticationPrincipal UserDetails userDetails) {

        Reservation reservation = reservationRepository.findById(id).orElseThrow();

        User loginUser = userRepository.findByEmail(userDetails.getUsername());

        if (loginUser == null) {
            return "redirect:/login";
        }

        // 自分の予約以外は削除させない
        if (!reservation.getUser().getId().equals(loginUser.getId())) {
            return "redirect:/reservations";
        }
        
        if (reservation.getReservedDate().isBefore(java.time.LocalDate.now())) {
            return "redirect:/reservations";
        }

        reservationRepository.delete(reservation);

        return "redirect:/reservations?reservationDeleted";
    }
    
   
}

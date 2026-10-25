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

	public ReservationController(ReservationRepository reservationRepository, UserRepository userRepository,
			ShopRepository shopRepository) {
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

		List<Reservation> reservations = reservationRepository.findByUserOrderByCreatedAtDesc(user);

		model.addAttribute("reservations", reservations);

		return "reservations/index";
	}

	// 予約作成画面（仮）
	@GetMapping("/new/{shopId}")
	public String newReservation(
			@PathVariable Long shopId,
			@AuthenticationPrincipal UserDetails userDetails,
			Model model,
			RedirectAttributes redirectAttributes) {

		System.out.println("【予約GET確認】shopId = " + shopId);

		if (userDetails == null) {
			return "redirect:/login";
		}

		User user = userRepository.findByEmail(userDetails.getUsername());

		if (user == null) {
			return "redirect:/login";
		}

		// 無料会員は予約画面を開けない
		if (!user.isPremium()) {
			redirectAttributes.addFlashAttribute(
					"error",
					"予約機能は有料会員限定です。");
			return "redirect:/shops/" + shopId;
		}

		model.addAttribute("shopId", shopId);

		return "reservations/new";
	}

	@PostMapping("/{shopId}")
	public String create(@PathVariable Long shopId,
			@RequestParam(required = false) String numPeople,
			@RequestParam(required = false) String reservationDate,
			@AuthenticationPrincipal UserDetails userDetails,
			RedirectAttributes redirectAttributes) { // redirectAttribute追加

		User user = userRepository.findByEmail(userDetails.getUsername());

		if (user == null) {
			return "redirect:/login";
		}
		System.out.println("【予約POST確認】email = " + user.getEmail());
		System.out.println("【予約画面テスト】premium = " + user.isPremium());
		// 無料会員は予約できない
		if (!user.isPremium()) {
			redirectAttributes.addFlashAttribute(
					"error",
					"予約機能は有料会員限定です。");
			return "redirect:/shops/" + shopId;
		}

		Shop shop = shopRepository.findById(shopId).orElseThrow();

		LocalDateTime dateTime;

		// 予約日時が未入力の場合
		if (reservationDate == null || reservationDate.isBlank()) {

			redirectAttributes.addFlashAttribute(
					"error",
					"予約日時を入力してください。");

			return "redirect:/reservations/new/" + shopId;
		}

		try {
			// 入力された予約日時を変換
			dateTime = LocalDateTime.parse(reservationDate);

		} catch (java.time.format.DateTimeParseException e) {

			// 日時の形式が不正な場合
			redirectAttributes.addFlashAttribute(
					"error",
					"予約日時を正しく入力してください。");

			return "redirect:/reservations/new/" + shopId;
		}

		// 人数制限(10人まで)
		// 人数が未入力の場合
		if (numPeople == null || numPeople.isBlank()) {
		    redirectAttributes.addFlashAttribute(
		            "error",
		            "予約人数を入力してください。"
		    );
		    return "redirect:/reservations/new/" + shopId;
		}

		// 人数を数値に変換
		int numberOfPeople;

		try {
		    numberOfPeople = Integer.parseInt(numPeople);
		} catch (NumberFormatException e) {
		    redirectAttributes.addFlashAttribute(
		            "error",
		            "予約人数は数字で入力してください。"
		    );
		    return "redirect:/reservations/new/" + shopId;
		}

		// 人数制限（1〜10人）
		if (numberOfPeople < 1 || numberOfPeople > 10) {
		    redirectAttributes.addFlashAttribute(
		            "error",
		            "人数は1人以上10人以下で入力してください。"
		    );
		    return "redirect:/reservations/new/" + shopId;
		}
		// 過去の日付での予約不可能
		if (dateTime.isBefore(LocalDateTime.now())) {
			redirectAttributes.addFlashAttribute("error", "過去の日時は選択できません");
			return "redirect:/reservations/new/" + shopId;
		}

		// 同じユーザー・店舗・日時の重複予約を防止
		boolean alreadyReserved = reservationRepository.existsByUserAndShopAndReservedDateAndReservedTime(
				user,
				shop,
				dateTime.toLocalDate(),
				dateTime.toLocalTime().toString());

		if (alreadyReserved) {
			redirectAttributes.addFlashAttribute(
					"error",
					"同じ店舗・日時ですでに予約しています。");

			return "redirect:/reservations/new/" + shopId;
		}

		Reservation reservation = new Reservation();
		reservation.setUser(user);
		reservation.setShop(shop);
		reservation.setReservedDate(dateTime.toLocalDate()); // 仮
		reservation.setReservedTime(dateTime.toLocalTime().toString()); // 仮
		reservation.setNumberOfPeople(numberOfPeople); // 仮

		reservationRepository.save(reservation);

		return "redirect:/reservations";
	}

	@PostMapping("/delete/{id}")
	public String deleteReservation(@PathVariable Long id,
			@AuthenticationPrincipal UserDetails userDetails) {

		Reservation reservation = reservationRepository.findById(id).orElse(null);

		if (reservation == null) {
		    return "redirect:/reservations";
		}

		User loginUser = userRepository.findByEmail(userDetails.getUsername());

		if (loginUser == null) {
			return "redirect:/login";
		}

		// 自分の予約以外は削除させない
		if (!reservation.getUser().getId().equals(loginUser.getId())) {
			return "redirect:/reservations";
		}

		// 予約当日以降はキャンセルできない
		if (!reservation.getReservedDate().isAfter(java.time.LocalDate.now())) {

			return "redirect:/reservations";
		}

		reservationRepository.delete(reservation);

		return "redirect:/reservations?reservationDeleted";
	}

}

package com.example.nagoyameshi.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nagoyameshi.entity.Reservation;
import com.example.nagoyameshi.entity.Shop;
import com.example.nagoyameshi.entity.User;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // 自分の予約一覧を取得
    List<Reservation> findByUserOrderByCreatedAtDesc(User user);

    // 同じユーザー・店舗・日付・時間の予約が存在するか確認
    boolean existsByUserAndShopAndReservedDateAndReservedTime(
            User user,
            Shop shop,
            LocalDate reservedDate,
            String reservedTime
    );
}
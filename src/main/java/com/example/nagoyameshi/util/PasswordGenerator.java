package com.example.nagoyameshi.util; // 管理者パスワード作成用データ

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

//管理者初期パスワード生成用（手動実行のみ）
public class PasswordGenerator {
	public static void main(String[] args) {
	    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
	    System.out.println(encoder.encode("admin1234"));
	}
}

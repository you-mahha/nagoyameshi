package com.example.nagoyameshi.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CategoryForm { // Formは画面入力専用のクラス

	 @NotBlank(message = "カテゴリー名を入力してください")
	    @Size(max = 50, message = "カテゴリー名は50文字以内で入力してください") // 長すぎを防止
	 
	private String name;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
}

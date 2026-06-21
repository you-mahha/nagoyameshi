package com.example.nagoyameshi.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SignupForm {

    @NotBlank(message="名前を入力してください")
    @Size(max=50,message="名前は50文字以内です")
    private String name;

    @NotBlank(message="メールアドレスを入力してください")
    @Email(message="メール形式が正しくありません")
    private String email;

    @NotBlank(message="パスワードを入力してください")
    @Size(min=8,max=100, message="パスワードは8文字以上です")
    private String password;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name=name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email=email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password=password;
    }
}
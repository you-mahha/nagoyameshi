package com.example.nagoyameshi.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role; // ROLE_USER, ROLE_ADMIN
    
    @Column(nullable = false)
    private boolean enabled = true;
    
 // ★ 新規追加
    @Column(nullable = false)
    private boolean premium = false;

    private LocalDate premiumValidUntil;
    
    @Column(nullable = false)
    private boolean cancelAtPeriodEnd = false;

    // getterとsetter
    public Long getId() { return id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    public boolean isPremium() { 

        // 有効期限未設定なら有料扱い
    	return premiumValidUntil != null
                && !LocalDate.now().isAfter(premiumValidUntil);
    }
    
    public void setPremium(boolean premium) { this.premium = premium; }

    public LocalDate getPremiumValidUntil() { return premiumValidUntil; }
    public void setPremiumValidUntil(LocalDate premiumValidUntil) { this.premiumValidUntil = premiumValidUntil; }

    
 // 自動更新対応　//
    @Column(unique = true)
    private String stripeCustomerId;

    @Column(unique = true)
    private String stripeSubscriptionId;
    
    
    public String getStripeCustomerId() {
        return stripeCustomerId;
    }

    public void setStripeCustomerId(String stripeCustomerId) {
        this.stripeCustomerId = stripeCustomerId;
    }

    public String getStripeSubscriptionId() {
        return stripeSubscriptionId;
    }

    public void setStripeSubscriptionId(String stripeSubscriptionId) {
        this.stripeSubscriptionId = stripeSubscriptionId;
    }

    public boolean isCancelAtPeriodEnd() {
        return cancelAtPeriodEnd;
    }

    public void setCancelAtPeriodEnd(boolean cancelAtPeriodEnd) {
        this.cancelAtPeriodEnd = cancelAtPeriodEnd;
    }
    
    
}
package com.example.nagoyameshi.controller;

import java.time.LocalDate;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.repository.UserRepository;
import com.stripe.Stripe;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.Invoice;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/payment")
public class WebhookController {

	@Autowired
	private UserRepository userRepository;

	@Value("${stripe.secret-key}")
    private String stripeApiKey;
	
	@PostMapping("/webhook")
	public ResponseEntity<String> webhook(HttpServletRequest request) throws Exception {

		// apiKey追加　//
		Stripe.apiKey = stripeApiKey;
		
		System.out.println("===== Webhook受信開始 =====");

		String payload = new String(request.getInputStream().readAllBytes());
		String sigHeader = request.getHeader("Stripe-Signature");

		System.out.println("署名ヘッダー: " + sigHeader);

		String endpointSecret = "whsec_c15659afecee3085d604a1c6857408b9d12271bece49e3dafbb5062c215e4e92"; // ←自分のに置き換え
		Event event;

		try {
			event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
			System.out.println("イベントタイプ: " + event.getType());
		} catch (Exception e) {
			System.out.println("❌ 署名エラー: " + e.getMessage());
			return ResponseEntity.ok("");
		}
		
		// =========================
		// checkout.session.completed
		// =========================
		if ("checkout.session.completed".equals(event.getType())) {

			System.out.println("✅ checkout.session.completed 検知");

			EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();

			Session session = null;
			// -------------------------
			// ① 通常取得
			// -------------------------
			if (dataObjectDeserializer.getObject().isPresent()) {

				System.out.println("✅ Session deserialize成功");

				session = (Session) dataObjectDeserializer.getObject().get();

			} else {

				// ② 失敗 → 再取得
				System.out.println("⚠️ Session deserialize失敗 → 再取得開始");

				try {
					// payloadから直接取得（安全）
					String sessionId = new JSONObject(payload)
							.getJSONObject("data")
							.getJSONObject("object")
							.getString("id");

					System.out.println("sessionId: " + sessionId);

					session = Session.retrieve(sessionId);

				} catch (Exception e) {
					System.out.println("❌ Session再取得失敗: " + e.getMessage());
				}
			}
			
			if (session != null) {
                saveStripeIds(session);
            } else {
                System.out.println("❌ session取得失敗");
            }
        }
		// invoice.paid //
			if ("invoice.paid".equals(event.getType())) {

			    System.out.println("🔄 invoice.paid 検知");

			    EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();

			    Invoice invoice = null;

			    if (dataObjectDeserializer.getObject().isPresent()) {
			    	System.out.println("✅ Invoice deserialize成功");
			        invoice = (Invoice) dataObjectDeserializer.getObject().get();
			    } else {
			    	System.out.println("⚠️ Invoice deserialize失敗 → 再取得開始");
			    	
			        try {
			            String invoiceId = new JSONObject(payload)
			                    .getJSONObject("data")
			                    .getJSONObject("object")
			                    .getString("id");
			        System.out.println("invoiceId: " + invoiceId);

			            invoice = Invoice.retrieve(invoiceId);
			        } catch (Exception e) {
			            System.out.println("❌ invoice再取得失敗: " + e.getMessage());
			        }
			    }

			    if (invoice != null) {
			        String customerId = invoice.getCustomer();
			        String subscriptionId = invoice.getSubscription();

			        System.out.println("customerId: " + customerId);
			        System.out.println("subscriptionId: " + subscriptionId);

			        updateSubscriptionByInvoice(customerId, subscriptionId);
			    } else {
			        System.out.println("❌ invoice取得失敗");
			    }
			}
			
			System.out.println("===== Webhook処理終了 =====");

			// =========================
			// customer.subscription.updated
			// =========================
			if ("customer.subscription.updated".equals(event.getType())) {

			    System.out.println("📌 subscription.updated 検知");

			    EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
			    Subscription subscription = null;

			    if (dataObjectDeserializer.getObject().isPresent()) {
			        subscription = (Subscription) dataObjectDeserializer.getObject().get();
			    } else {
			        try {
			            String subscriptionId = new JSONObject(payload)
			                    .getJSONObject("data")
			                    .getJSONObject("object")
			                    .getString("id");

			            subscription = Subscription.retrieve(subscriptionId);
			        } catch (Exception e) {
			            System.out.println("❌ subscription再取得失敗: " + e.getMessage());
			        }
			    }

			    if (subscription != null) {
			        updateCancelStatus(subscription);
			    }
			}


			// =========================
			// customer.subscription.deleted
			// =========================
			if ("customer.subscription.deleted".equals(event.getType())) {

			    System.out.println("🗑 subscription.deleted 検知");

			    EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
			    Subscription subscription = null;

			    if (dataObjectDeserializer.getObject().isPresent()) {
			        subscription = (Subscription) dataObjectDeserializer.getObject().get();
			    } else {
			        try {
			            String subscriptionId = new JSONObject(payload)
			                    .getJSONObject("data")
			                    .getJSONObject("object")
			                    .getString("id");

			            subscription = Subscription.retrieve(subscriptionId);
			        } catch (Exception e) {
			            System.out.println("❌ subscription再取得失敗: " + e.getMessage());
			        }
			    }

			    if (subscription != null) {
			        finishCancellation(subscription);
			    }
			    
			}
			System.out.println("===== Webhook処理終了 =====");
			return ResponseEntity.ok("");
		}
			// 初回の契約時にStripeのID保存
	private void saveStripeIds(Session session) {

		try {
			String userId = session.getClientReferenceId();
			System.out.println("clientReferenceId(userId): " + userId);
			System.out.println("session.getCustomer(): " + session.getCustomer());
	        System.out.println("session.getSubscription(): " + session.getSubscription());

			
			if (userId == null) {
				System.out.println("❌ userIdがnullです");
				return;
			}

			userRepository.findById(Long.valueOf(userId)).ifPresentOrElse(user -> {
    
	            if (session.getCustomer() != null) {
	            	user.setStripeCustomerId(session.getCustomer());
	            }
	            
	            if (session.getSubscription() != null) {
	            	user.setStripeSubscriptionId(session.getSubscription());
	            }
	            
				userRepository.save(user);
				 System.out.println("✅ Stripe ID保存完了");
				 System.out.println("✅ customerId: " + user.getStripeCustomerId());
				 System.out.println("✅ subscriptionId: " + user.getStripeSubscriptionId());

			}, () -> {
				System.out.println("❌ ユーザーが見つかりません");
			});

		} catch (Exception e) {
			System.out.println("❌ Stripe ID保存失敗: " + e.getMessage());
			e.printStackTrace();
		}
	}
	// invoice.paidで有効期限を更新 //
	private void updateSubscriptionByInvoice(String customerId, String subscriptionId) {

	    try {
	        User user = null;

	        if (subscriptionId != null) {
	            user = userRepository.findByStripeSubscriptionId(subscriptionId);
	        }

	        if (user == null && customerId != null) {
	            user = userRepository.findByStripeCustomerId(customerId);
	        }

	        if (user == null) {
	            System.out.println("❌ invoice.paid に対応するユーザーが見つかりません");
	            return;
	        }

	        LocalDate now = LocalDate.now();

	        if (user.getPremiumValidUntil() != null &&
	            !now.isAfter(user.getPremiumValidUntil())) {
	            user.setPremiumValidUntil(user.getPremiumValidUntil().plusMonths(1));
	        } else {
	            user.setPremiumValidUntil(now.plusMonths(1));
	        }

	        user.setPremium(true);
	        userRepository.save(user);

	        System.out.println("✅ 自動更新後の有効期限: " + user.getPremiumValidUntil());

	    } catch (Exception e) {
	        System.out.println("❌ invoice.paid 更新失敗: " + e.getMessage());
	    }
	}
	// 解約予定を更新 //
	private void updateCancelStatus(Subscription subscription) {
	    try {
	        String subscriptionId = subscription.getId();

	        User user = userRepository.findByStripeSubscriptionId(subscriptionId);

	        if (user == null) {
	            System.out.println("❌ ユーザー見つからない");
	            return;
	        }

	        user.setCancelAtPeriodEnd(subscription.getCancelAtPeriodEnd());
	        userRepository.save(user);

	        System.out.println("✅ 解約予定更新: " + user.isCancelAtPeriodEnd());

	    } catch (Exception e) {
	        System.out.println("❌ update失敗: " + e.getMessage());
	    }
	}
// 解約完了後無料会員へ戻す
	private void finishCancellation(Subscription subscription) {
	    try {
	        String subscriptionId = subscription.getId();

	        User user = userRepository.findByStripeSubscriptionId(subscriptionId);

	        if (user == null) {
	            System.out.println("❌ ユーザー見つからない");
	            return;
	        }

	        user.setPremium(false);
	        user.setPremiumValidUntil(null);
	        user.setCancelAtPeriodEnd(false);
	        user.setStripeSubscriptionId(null);

	        userRepository.save(user);

	        System.out.println("✅ 解約完了");

	    } catch (Exception e) {
	        System.out.println("❌ 解約処理失敗: " + e.getMessage());
	    }
	}
}
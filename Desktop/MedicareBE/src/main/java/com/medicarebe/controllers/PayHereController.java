package com.medicarebe.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PayHereController {

    private static final String SANDBOX_MERCHANT_ID = "1234122";
    private static final String MERCHANT_SECRET = "MjEwOTEwMTUyNDE5NzMwOTc5MDIxNTM3NzE1NDkzOTM0NDcxNDI=";

    // ✅ Step 2: Hash endpoint
    @GetMapping("/get-payhere-hash")
    public Map<String, String> getHash(
            @RequestParam String order_id,
            @RequestParam String amount,
            @RequestParam String currency) {

        String secretMd5 = DigestUtils.md5DigestAsHex(MERCHANT_SECRET.getBytes(StandardCharsets.UTF_8)).toUpperCase();
        String formattedAmount = String.format("%.2f", Double.parseDouble(amount));
        String hashInput = SANDBOX_MERCHANT_ID + order_id + formattedAmount + currency + secretMd5;
        String hash = DigestUtils.md5DigestAsHex(hashInput.getBytes(StandardCharsets.UTF_8)).toUpperCase();

        return Map.of("hash", hash);
    }

    // ✅ Step 3: PayHere Payment Notification
    @PostMapping("/payhere-notify")
    public String handlePaymentNotification(
            @RequestParam String merchant_id,
            @RequestParam String order_id,
            @RequestParam String payment_id,
            @RequestParam String payhere_amount,
            @RequestParam String payhere_currency,
            @RequestParam String status_code,
            @RequestParam String md5sig
    ) {

        // 1️⃣ Generate local MD5 signature
        String localMd5SigInput = merchant_id + order_id + payhere_amount + payhere_currency + status_code +
                DigestUtils.md5DigestAsHex(MERCHANT_SECRET.getBytes(StandardCharsets.UTF_8)).toUpperCase();
        String localMd5Sig = DigestUtils.md5DigestAsHex(localMd5SigInput.getBytes(StandardCharsets.UTF_8)).toUpperCase();

        // 2️⃣ Verify hash
        if (!localMd5Sig.equals(md5sig)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid MD5 signature");
        }

        // 3️⃣ Check payment status
        switch (status_code) {
            case "2": // SUCCESS
                System.out.println("Payment SUCCESS for OrderID: " + order_id + ", PaymentID: " + payment_id);

                // 🔥 Save payment to DB (Firebase/MySQL)
                // Example: paymentService.save(order_id, payment_id, payhere_amount, "Paid");

                break;
            case "0": System.out.println("Payment PENDING for OrderID: " + order_id); break;
            case "-1": System.out.println("Payment CANCELLED for OrderID: " + order_id); break;
            case "-2": System.out.println("Payment FAILED for OrderID: " + order_id); break;
            case "-3": System.out.println("Payment CHARGEDBACK for OrderID: " + order_id); break;
        }

        return "OK"; // Must return 200 to PayHere
    }

    // ✅ Step 4: Refund API
    @PostMapping("/payment/refund")
    public ResponseEntity<?> refundPayment(@RequestBody RefundRequest request) {
        try {
            String paymentId = request.getPaymentId();
            String amount = request.getAmount();

            RestTemplate restTemplate = new RestTemplate();
            String url = "https://sandbox.payhere.lk/merchant/v1/payment/refund";

            Map<String, String> body = new HashMap<>();
            body.put("merchant_id", SANDBOX_MERCHANT_ID);
            body.put("merchant_secret", MERCHANT_SECRET);
            body.put("payment_id", paymentId);
            body.put("amount", amount);

            ResponseEntity<String> response = restTemplate.postForEntity(url, body, String.class);

            // 🔥 After refund success: update DB
            // paymentService.updateStatus(paymentId, "Refunded");

            return ResponseEntity.ok("Refund request sent: " + response.getBody());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Refund failed: " + e.getMessage());
        }
    }

}

// DTO for Refund
class RefundRequest {
    private String paymentId;
    private String amount;

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getAmount() { return amount; }
    public void setAmount(String amount) { this.amount = amount; }
}
package com.medicarebe.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // 🔹 Doctor credentials email
    public void sendDoctorCredentials(String toEmail, String doctorName, String password) {
        String subject = "Your Doctor Account Credentials";
        String body = "Hello " + doctorName + ",\n\n" +
                "Your account has been created. Here are your credentials:\n" +
                "Email: " + toEmail + "\n" +
                "Password: " + password + "\n\n" +
                "Please log in and change your password immediately.\n\n" +
                "Best regards,\nMediCare Admin Team";

        sendEmail(toEmail, subject, body);
    }

    // 🔹 Pharmacy credentials email
    public void sendPharmacyCredentials(String toEmail, String pharmacyName, String password) {
        String subject = "Your Pharmacy Account Credentials";
        String body = "Hello " + pharmacyName + ",\n\n" +
                "Your account has been created. Here are your credentials:\n" +
                "Email: " + toEmail + "\n" +
                "Password: " + password + "\n\n" +
                "Please log in and change your password immediately.\n\n" +
                "Best regards,\nMediCare Admin Team";

        sendEmail(toEmail, subject, body);
    }

    // 🔹 Generic method to send emails
    private void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        message.setFrom("medicare615@gmail.com");
        mailSender.send(message);
    }

    @PostMapping("/sendOrderAcceptedEmail")
    public ResponseEntity<?> sendOrderAcceptedEmail(@RequestBody Map<String, String> payload) {
        // Expecting payload from frontend: patientEmail, patientName, orderId
        String patientEmail = payload.get("patientEmail");
        String patientName = payload.get("patientName");
        String orderId = payload.get("orderId");

        if(patientEmail == null || patientName == null || orderId == null) {
            return ResponseEntity.badRequest().body("Missing order data");
        }

        sendOrderAccepted(patientEmail, patientName, orderId);
        return ResponseEntity.ok("Email sent");
    }

    // This method actually sends the email
    public void sendOrderAccepted(String toEmail, String patientName, String orderId) {
        String subject = "Your Order Has Been Accepted!";
        String body = "Hello " + patientName + ",\n\n" +
                "Your order (ID: " + orderId + ") has been accepted by the pharmacy.\n\n" +
                "Thank you for using MediCare!\n\nBest regards,\nMediCare Admin Team";
        sendEmail(toEmail, subject, body);
    }

    // Endpoint to send email on appointment status change
    @PostMapping("/sendAppointmentStatusEmail")
    public ResponseEntity<?> sendAppointmentStatusEmail(@RequestBody Map<String, String> payload) {
        String patientEmail = payload.get("patientEmail");
        String patientName = payload.get("patientName");
        String appointmentId = payload.get("appointmentId");
        String status = payload.get("status");

        if(patientEmail == null || patientName == null || appointmentId == null || status == null) {
            return ResponseEntity.badRequest().body("Missing appointment data");
        }

        sendAppointmentStatus(patientEmail, patientName, appointmentId, status);
        return ResponseEntity.ok("Email sent");
    }

    // Actually sends the email
    public void sendAppointmentStatus(String toEmail, String patientName, String appointmentId, String status) {
        String subject = "Your Appointment Has Been " + (status.equals("approved") ? "Approved" : "Rejected") + "!";
        String body = "Hello " + patientName + ",\n\n" +
                "Your appointment (ID: " + appointmentId + ") has been " + status + " by the doctor.\n\n" +
                "Thank you for using MediCare!\n\nBest regards,\nMediCare Admin Team";

        sendEmail(toEmail, subject, body);
    }

    public void sendOrderStatus(String toEmail, String patientName, String orderId, String status) {
        if(toEmail == null || patientName == null || orderId == null || status == null) {
            System.err.println("Missing email parameters");
            return;
        }

        String subject;
        String body;

        if(status.equalsIgnoreCase("accepted")) {
            subject = "Your Order Has Been Accepted!";
            body = "Hello " + patientName + ",\n\n" +
                    "Your order (ID: " + orderId + ") has been accepted by the pharmacy.\n\n" +
                    "Thank you for using MediCare!\n\nBest regards,\nMediCare Admin Team";
        } else if(status.equalsIgnoreCase("rejected")) {
            subject = "Your Order Has Been Rejected";
            body = "Hello " + patientName + ",\n\n" +
                    "Your order (ID: " + orderId + ") has been rejected by the pharmacy.\n\n" +
                    "Thank you for using MediCare!\n\nBest regards,\nMediCare Admin Team";
        } else {
            System.err.println("Unknown order status: " + status);
            return;
        }

        sendEmail(toEmail, subject, body);
    }

    // 🔹 Controller endpoint for frontend to call
    @PostMapping("/sendOrderStatusEmail")
    public ResponseEntity<?> sendOrderStatusEmail(@RequestBody Map<String, String> payload) {
        String patientEmail = payload.get("patientEmail");
        String patientName = payload.get("patientName");
        String orderId = payload.get("orderId");
        String status = payload.get("status"); // accepted / rejected

        if(patientEmail == null || patientName == null || orderId == null || status == null) {
            return ResponseEntity.badRequest().body("Missing order data");
        }

        sendOrderStatus(patientEmail, patientName, orderId, status);
        return ResponseEntity.ok("Email sent successfully");
    }
}
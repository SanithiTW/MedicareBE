package com.medicarebe.controllers;

import com.medicarebe.Service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/email")

public class AppoinmentController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/sendAppointmentStatusEmail")
    public ResponseEntity<?> sendAppointmentStatusEmail(@RequestBody Map<String, String> payload) {
        return emailService.sendAppointmentStatusEmail(payload);
    }

    @PostMapping("/sendOrderAcceptedEmail")
    public ResponseEntity<?> sendOrderAcceptedEmail(@RequestBody Map<String, String> payload) {
        return emailService.sendOrderAcceptedEmail(payload);
    }
}
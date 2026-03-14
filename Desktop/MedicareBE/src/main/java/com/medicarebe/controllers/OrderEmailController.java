package com.medicarebe.controllers;

import com.medicarebe.Service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/email")
public class OrderEmailController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/sendOrderStatusEmail")
    public ResponseEntity<?> sendOrderStatusEmail(@RequestBody Map<String, String> payload) {
        return emailService.sendOrderStatusEmail(payload);
    }
}
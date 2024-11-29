package com.binhcodev.order_service.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.binhcodev.order_service.services.impl.PayPalService;

@RestController
@RequestMapping("/api/payments")
public class PayPalController {

    private final PayPalService payPalService;

    public PayPalController(PayPalService payPalService) {
        this.payPalService = payPalService;
    }

    @PostMapping("/create")
    public ResponseEntity<String> createPayment(
            @RequestParam Double total,
            @RequestParam String currency,
            @RequestParam String description,
            @RequestParam String cancelUrl,
            @RequestParam String successUrl) {
        String approvalUrl = payPalService.createPayment(total, currency, description, cancelUrl, successUrl);
        return ResponseEntity.ok(approvalUrl);
    }
}

package com.binhcodev.order_service.services.impl;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.binhcodev.order_service.configs.PayPalConfig;

@Service
public class PayPalService {
    private static final String[] DOMAINS = { "example.com" };
    private final PayPalConfig payPalConfig;
    private final RestTemplate restTemplate;

    public PayPalService(PayPalConfig payPalConfig, RestTemplate restTemplate) {
        this.payPalConfig = payPalConfig;
        this.restTemplate = restTemplate;
    }

    public String createPayment(Double total, String currency, String description, String cancelUrl,
            String successUrl) {
        String accessToken = getAccessToken();

        // Create payment request payload
        Map<String, Object> paymentPayload = new HashMap<>();
        paymentPayload.put("intent", "sale");

        Map<String, String> redirectUrls = new HashMap<>();
        redirectUrls.put("return_url", successUrl);
        redirectUrls.put("cancel_url", cancelUrl);
        paymentPayload.put("redirect_urls", redirectUrls);

        List<Map<String, Object>> transactions = new ArrayList<>();
        Map<String, Object> transactionDetails = new HashMap<>();
        transactionDetails.put("amount", Map.of("total", total, "currency", currency));
        transactionDetails.put("description", description);
        transactions.add(transactionDetails);
        paymentPayload.put("transactions", transactions);

        Map<String, String> payer = Map.of("payment_method", "paypal");
        paymentPayload.put("payer", payer);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(paymentPayload, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(payPalConfig.getBaseUrl() + "/v1/payments/payment",
                request, Map.class);

        // Handle and return approval URL
        if (response.getBody() != null) {
            List<Map<String, String>> links = (List<Map<String, String>>) response.getBody().get("links");
            return links.stream()
                    .filter(link -> "approval_url".equals(link.get("rel")))
                    .map(link -> link.get("href"))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Approval URL not found"));
        }
        throw new RuntimeException("Payment creation failed.");
    }

    private String getAccessToken() {
        String credentials = payPalConfig.getClientId() + ":" + payPalConfig.getClientSecret();
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(encodedCredentials);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<String> request = new HttpEntity<>("grant_type=client_credentials", headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                payPalConfig.getBaseUrl() + "/v1/oauth2/token", HttpMethod.POST, request,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });
        if (response.getBody() != null) {
            return (String) response.getBody().get("access_token");
        }
        throw new RuntimeException("Access Token request failed.");
    }

    private String getToken(UriComponentsBuilder uriBuilder, String responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(payPalConfig.getClientId(), payPalConfig.getClientSecret());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                uriBuilder.build().toUri(), HttpMethod.POST, request,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });

        if (response.getBody() != null) {
            return (String) response.getBody().get(responseType);
        }
        throw new RuntimeException("Token request failed.");
    }

    public String getClientToken() {
        UriComponentsBuilder bodyUriBuilder = UriComponentsBuilder.newInstance()
                .queryParam("grant_type", "client_credentials")
                .queryParam("response_type", "client_token")
                .queryParam("intent", "sdk_init")
                .queryParam("domains[]", (Object[]) PayPalService.DOMAINS);

        String clientToken = this.getToken(bodyUriBuilder, "client_token");

        return clientToken;
    }
}

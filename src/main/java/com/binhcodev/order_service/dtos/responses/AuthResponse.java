package com.binhcodev.order_service.dtos.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

class AuthResponse {
    @JsonProperty("access_token")
    private String accessToken;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String clientToken) {
        this.accessToken = clientToken;
    }
}

package com.binhcodev.order_service.dtos.responses;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ClientTokenResponse {
    private String clientToken;
    private String clientId;
    private String paypalSdkBaseUrl;
}

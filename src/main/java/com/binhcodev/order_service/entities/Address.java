package com.binhcodev.order_service.entities;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
class Address {
    private String addressLine1;
    private String addressLine2;
    private String adminArea2;
    private String adminArea1;
    private String postalCode;
    private String countryCode;
}

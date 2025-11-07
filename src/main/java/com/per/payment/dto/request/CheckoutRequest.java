package com.per.payment.dto.request;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutRequest {

    private List<UUID> cartItemIds;

    @NotBlank(message = "Receiver name is required")
    private String receiverName;

    @NotBlank(message = "Receiver phone is required")
    private String receiverPhone;

    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    private String note;

    public boolean hasSelectedItems() {
        return cartItemIds != null && !cartItemIds.isEmpty();
    }
}

package com.per.cart.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.per.cart.dto.response.CartItemResponse;
import com.per.cart.dto.response.CartResponse;
import com.per.cart.entity.Cart;
import com.per.cart.entity.CartItem;

@Mapper(componentModel = "spring")
public interface CartMapper {

    @Mapping(target = "userId", source = "user.id")
    CartResponse toResponse(Cart cart);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productImageUrl", source = "product.imageUrl")
    @Mapping(target = "variantId", source = "productVariant.id")
    @Mapping(target = "variantSku", source = "productVariant.variantSku")
    @Mapping(target = "variantVolumeMl", source = "productVariant.volumeMl")
    @Mapping(target = "variantPackageType", source = "productVariant.packageType")
    @Mapping(target = "variantImageUrl", source = "productVariant.imageUrl")
    CartItemResponse toItemResponse(CartItem item);
}

package com.example.cart.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PromoCodeDiscountDto {
  private final String code;
  private final BigDecimal totalDiscount;
}

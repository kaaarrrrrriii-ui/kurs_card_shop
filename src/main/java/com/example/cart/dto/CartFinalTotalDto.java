package com.example.cart.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CartFinalTotalDto {
  private final Long cartId;
  private final String promoCode;
  private final BigDecimal total;
}

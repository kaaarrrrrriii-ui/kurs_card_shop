package com.example.cart.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CartTotalDto {
  private final Long cartId;
  private final BigDecimal total;
}

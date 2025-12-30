package com.example.cart.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CartEfficiencyDto {
  private final Long cartId;
  private final BigDecimal total;
  private final long itemCount;
  private final long distinctProducts;
  private final BigDecimal score;
}

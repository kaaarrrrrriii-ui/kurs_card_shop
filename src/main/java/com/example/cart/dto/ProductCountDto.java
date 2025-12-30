package com.example.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductCountDto {
  private final Long productId;
  private final String name;
  private final long totalQuantity;
}

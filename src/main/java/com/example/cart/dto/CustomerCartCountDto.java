package com.example.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomerCartCountDto {
  private final Long customerId;
  private final String email;
  private final long activeCarts;
}

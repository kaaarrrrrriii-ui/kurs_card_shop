package com.example.cart.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "promo_codes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PromoCode {
  @Id
  private String code;

  @Enumerated(EnumType.STRING)
  private DiscountType discountType;

  private BigDecimal discountValue;

  private boolean active;

  private String applicableCategory;
}

package com.example.cart.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "carts")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Cart {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long cartId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "customer_id")
  private Customer customer;

  private LocalDateTime createdDate;

  @Column(name = "is_active")
  private boolean active;

  @Column(name = "promo_code")
  private String promoCode;

  @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<CartItem> items = new ArrayList<>();

  public String getEntityLabel() {
    return "Корзина";
  }

  public void updateCustomer(Customer customer) {
    this.customer = customer;
  }

  public void updateCreatedDate(LocalDateTime createdDate) {
    this.createdDate = createdDate;
  }

  public void updateActive(boolean active) {
    this.active = active;
  }

  public void updatePromoCode(String promoCode) {
    this.promoCode = normalizePromoCode(promoCode);
  }

  private String normalizePromoCode(String promoCode) {
    if (promoCode == null || promoCode.isBlank()) {
      return null;
    }
    return promoCode.trim();
  }
}

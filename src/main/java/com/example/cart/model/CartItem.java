package com.example.cart.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cart_items")
@PrimaryKeyJoinColumn(name = "product_id")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CartItem extends Product {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cart_id")
  private Cart cart;

  @Column(name = "source_product_id")
  private Long sourceProductId;

  private int quantity;

  private LocalDateTime addedDate;

  public String getEntityLabel() {
    return "Позиция корзины";
  }

  public void updateCart(Cart cart) {
    this.cart = cart;
  }

  public void updateProductSnapshot(Product product) {
    if (product == null) {
      throw new IllegalArgumentException("Product is required");
    }
    this.sourceProductId = product.getProductId();
    setName(product.getName());
    setCategory(product.getCategory());
    setBasePrice(product.getBasePrice());
    setInStock(product.isInStock());
  }

  public void updateQuantity(int quantity) {
    if (quantity <= 0) {
      throw new IllegalArgumentException("Quantity must be positive");
    }
    this.quantity = quantity;
  }

  public void updateAddedDate(LocalDateTime addedDate) {
    this.addedDate = addedDate;
  }

  public Long getCartItemId() {
    return getProductId();
  }
}


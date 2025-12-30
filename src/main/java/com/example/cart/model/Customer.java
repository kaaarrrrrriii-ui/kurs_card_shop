package com.example.cart.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long customerId;

  private String email;

  private LocalDate registrationDate;

  @Enumerated(EnumType.STRING)
  private LoyaltyLevel loyaltyLevel;

  @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private List<Cart> carts = new ArrayList<>();
}

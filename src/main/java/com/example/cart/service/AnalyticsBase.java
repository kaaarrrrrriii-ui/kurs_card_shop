package com.example.cart.service;

public abstract class AnalyticsBase implements CategoryNormalizer {
  public String normalizeCategory(String category) {
    if (category == null || category.isBlank()) {
      return "UNSPECIFIED";
    }
    return category;
  }
}

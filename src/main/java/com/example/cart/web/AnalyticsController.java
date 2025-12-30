package com.example.cart.web;

import com.example.cart.service.CartAnalyticsService;
import com.example.cart.service.ViewDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AnalyticsController {
  private final CartAnalyticsService service;
  private final ViewDataService viewDataService;

  @GetMapping("/")
  public String index(Model model) {
    viewDataService.populateLists(model);
    return "index";
  }

  @PostMapping("/analytics/active-cart-totals")
  public String activeCartTotals(Model model) {
    model.addAttribute("activeTab", "analytics");
    model.addAttribute("activeCartTotals", service.calculateActiveCartTotals());
    viewDataService.populateLists(model);
    return "index";
  }

  @PostMapping("/analytics/top-products")
  public String topProducts(Model model) {
    model.addAttribute("activeTab", "analytics");
    model.addAttribute("topProducts", service.top5ProductsByAdditions());
    viewDataService.populateLists(model);
    return "index";
  }

  @PostMapping("/analytics/active-carts-per-customer")
  public String activeCartsPerCustomer(Model model) {
    model.addAttribute("activeTab", "analytics");
    model.addAttribute("activeCartsPerCustomer", service.activeCartCountPerCustomer());
    viewDataService.populateLists(model);
    return "index";
  }

  @PostMapping("/analytics/category-counts")
  public String categoryCounts(Model model) {
    model.addAttribute("activeTab", "analytics");
    model.addAttribute("categoryCounts", service.categoryItemCounts());
    viewDataService.populateLists(model);
    return "index";
  }

  @PostMapping("/analytics/final-totals-with-promo")
  public String finalTotalsWithPromo(Model model) {
    model.addAttribute("activeTab", "analytics");
    model.addAttribute("finalTotalsWithPromo", service.finalTotalsWithPromo());
    viewDataService.populateLists(model);
    return "index";
  }

  @PostMapping("/analytics/unused-promos")
  public String unusedPromos(Model model) {
    model.addAttribute("activeTab", "analytics");
    model.addAttribute("unusedPromos", service.activePromoCodesNeverUsed());
    viewDataService.populateLists(model);
    return "index";
  }

  @PostMapping("/analytics/promo-discount-summary")
  public String promoDiscountSummary(Model model) {
    model.addAttribute("activeTab", "analytics");
    model.addAttribute("promoDiscountSummary", service.totalDiscountIfAppliedToEligibleCarts());
    viewDataService.populateLists(model);
    return "index";
  }

  @PostMapping("/analytics/out-of-stock-products")
  public String outOfStockProducts(Model model) {
    model.addAttribute("activeTab", "analytics");
    model.addAttribute("outOfStockProducts", service.productsInCartsOutOfStock());
    viewDataService.populateLists(model);
    return "index";
  }

  @PostMapping("/analytics/weighted-efficiency")
  public String weightedEfficiency(Model model) {
    model.addAttribute("activeTab", "analytics");
    model.addAttribute("weightedEfficiency", service.weightedCartEfficiencyScores());
    viewDataService.populateLists(model);
    return "index";
  }
}

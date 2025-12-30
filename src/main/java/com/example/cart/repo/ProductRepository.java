package com.example.cart.repo;

import com.example.cart.model.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {
  @Query("select p from Product p where type(p) = Product")
  List<Product> findAllProducts();

  @Query("select p from Product p where type(p) = Product and p.productId = :id")
  Optional<Product> findProductById(@Param("id") Long id);

  @Query("select case when count(p) > 0 then true else false end "
      + "from Product p where type(p) = Product and p.productId = :id")
  boolean existsProductById(@Param("id") Long id);
}

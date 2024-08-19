package com.ecommerce.project.repository;

import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
  Page<Product> findByCategoryOrderByPriceAsc(Category category, Pageable pageable);

  Product findByProductName(String productName);

  Page<Product> findByProductNameLikeIgnoreCase(String keyword, Pageable pageDetails);
}

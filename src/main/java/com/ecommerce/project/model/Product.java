package com.ecommerce.project.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long productId;
  @NotBlank
  @Size(min = 3, message = "Product Name should have at least 3 letter")
  private String productName;

  private String image;

  @NotBlank
  @Size(min = 6, message = "Product Name should have at least 6 letter")
  private String description;

  private Integer quantity;
  private double price;
  private double discount;
  private double spacialPrice;

  @ManyToOne
  @JoinColumn(name = "category_id")
  private Category category;
}
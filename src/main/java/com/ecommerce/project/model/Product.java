package com.ecommerce.project.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products")
@ToString
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

  @ManyToOne
  @JoinColumn(name = "seller_id")
  private Users users;

  @OneToMany(mappedBy = "product", cascade = {CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.MERGE}, fetch = FetchType.EAGER)
  private List<CartItem> cartItems;
}

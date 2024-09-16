package com.ecommerce.project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long orderItemId;

  @ManyToOne
  @JoinColumn(name = "order_id")
  private Orders order;

  @ManyToOne
  @JoinColumn(name = "product_id")
  private Product product;

  private int quantity;
  private double discount;
  private double orderedProductPrice;
}

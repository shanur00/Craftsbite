package com.ecommerce.project.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDTO {
  private Long orderItemId;
  private ProductDTO product;
  private Integer quantity;
  private double discount;
  private double orderedProductPrice;
}

package com.ecommerce.project.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {
  private Long cartId;
  private Double totalPrice=0.0;

  /*
     Here If a product is added to a cart, the all of it Information will be in the response.
     Example:
         {
           "cartId": 1,
           "totalPrice": 39.1804,
           "products": [
             {
               "productId": 1,
               "productName": "Travel Pillow 2",
               "image": null,
               "description": null,
               "quantity": 1,
               "price": 19.99,
               "discount": 2.0,
               "spacialPrice": 0.0
             }
           ]
         }
   */
  private List<ProductDTO> products = new ArrayList<>();
}

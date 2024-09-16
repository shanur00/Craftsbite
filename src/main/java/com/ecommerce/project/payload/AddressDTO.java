package com.ecommerce.project.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressDTO {
  private Long id;
  private String street;
  private String buildingName;
  private String city;
  private String state;
  private String country;
  private String zipCode;
}

package com.ecommerce.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Payment {
  @Id
  @GeneratedValue (strategy = GenerationType.IDENTITY)
  private int paymentId;

  @OneToOne(mappedBy = "payment", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
  private Orders order;

  @NotBlank
  @Size(min = 4, message = "Must be at least 4 character")
  private String paymentMethod;

  private String pgPaymentId;
  private String pgStatus;
  private String pgResponseMessage;
  private String pgName;

  public Payment(String paymentMethod, String pgPaymentId, String pgStatus,
                 String pgResponseMessage, String pgName) {
    this.paymentMethod = paymentMethod;
    this.pgPaymentId = pgPaymentId;
    this.pgStatus = pgStatus;
    this.pgResponseMessage = pgResponseMessage;
    this.pgName = pgName;
  }
}
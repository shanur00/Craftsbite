package com.ecommerce.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Orders {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long orderId;

  @Email
  @Column(nullable = false)
  private String email;

  @OneToMany(mappedBy = "order", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
  private List<OrderItem> orderItems = new ArrayList<>();

  @OneToOne
  @JoinColumn(name = "payment_id")
  private Payment payment;

  private LocalDate orderDate;

  private Double totalAmount;
  private String orderStatus;

  @ManyToOne
  @JoinColumn(name = "address_id")
  private Address address;
}

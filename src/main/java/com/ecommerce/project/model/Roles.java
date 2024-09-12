package com.ecommerce.project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "roles")

public class Roles {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "role_id")
  private Integer roleId;

  @Enumerated(EnumType.STRING)
  @Column(length = 20, name = "role_name")
  private AppRole roleName;

  public Roles(AppRole roleName) {
    this.roleName = roleName;
  }
}

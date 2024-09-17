package com.ecommerce.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@Table(name = "users",
  uniqueConstraints = {
  @UniqueConstraint(columnNames = "username"),
  @UniqueConstraint(columnNames = "email")
})

public class Users {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_id")
  private Long id;

  @NotBlank
  @Size(max = 20)
  @Column(name = "username")
  private String userName;

  @NotBlank
  @Size(max = 50)
  @Email
  @Column(name = "email")
  private String email;

  @NotBlank
  @Size(max = 120)
  @Column(name = "password")
  private String password;

  @Column(name = "enabled")
  private boolean enabled;

  public Users(String username, String email, String password) {
    this.userName = username;
    this.email = email;
    this.password = password;
    this.enabled = true;
  }

  @Getter
  @Setter
  @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE},
  fetch = FetchType.EAGER)
  @JoinTable(
    name = "authorities",
    joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns = @JoinColumn(name = "role_id")
  )
  private Set<Roles> rolesInUsers = new HashSet<>();

  @ToString.Exclude
  @OneToMany(mappedBy = "users",
    cascade = {CascadeType.PERSIST, CascadeType.MERGE},
    orphanRemoval = true)
  private Set<Product> products;

  @Getter
  @OneToMany(mappedBy = "users", cascade = {
    CascadeType.PERSIST, CascadeType.MERGE
  }, orphanRemoval = true)
  private List<Address> addressesInUsers = new ArrayList<>();

  @ToString.Exclude
  @OneToOne(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
  orphanRemoval = true)
  private Cart cart;
}

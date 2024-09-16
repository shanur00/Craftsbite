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
  /*
    uniqueConstraints = {}:
        This attribute defines one or more constraints to ensure that certain columns have unique values in the table.

        A "unique constraint" ensures that no two rows in the table can have the same value for the specified column(s).
        If a duplicate value is inserted into one of these columns, the database will raise an error.
  */
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

  /*
     Logical Reasons for Unidirectional Relationship:
             In an e-commerce platform, you typically need to know the roles of a user to determine permissions and access levels.
             However, you rarely need to find all users with a specific role directly from the Roles entity.

             Example:
                 When a user logs in, you check their roles to grant access to various parts of the application.
   */
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
  /*
      Users know which products they own (Set<Product> products).
      Products know which user (seller) owns them (@ManyToOne Users users).

      For example, a user might want to view all the products they own, and a product might need to show the seller’s information.

      Database Tables:
          users table
              user_id (PK)
              username
              email
              password

          products table
              product_id (PK)
              product_name
              seller_id (FK to users.user_id) [Owning class will control foreign Key]
   */
  @OneToMany(mappedBy = "users",
    cascade = {CascadeType.PERSIST, CascadeType.MERGE},
    /*
      Automatically delete child entities that are no longer associated with their parent.

      Difference from CascadeType.REMOVE:

      CascadeType.REMOVE:
         When you delete the parent entity, the CascadeType.REMOVE will delete all the associated child entities automatically.
         This is typically used when the parent entity is being deleted, and you want the deletion to cascade down to the children.

      orphanRemoval = true:
         This does not rely on the deletion of the parent entity but rather handles the scenario where a child entity is no
         longer referenced by its parent. It's about removing "orphaned" child entities rather than removing everything
         when the parent is deleted.
    */
    orphanRemoval = true)
  private Set<Product> products;

  @Getter
  @OneToMany(mappedBy = "users", cascade = {
    CascadeType.PERSIST, CascadeType.MERGE
  }, orphanRemoval = true)
//  @JoinTable(
//    name = "user_address",
//    joinColumns = @JoinColumn(name = "user_id"),
//    inverseJoinColumns = @JoinColumn(name = "address_id")
//  )
  private List<Address> addressesInUsers = new ArrayList<>();

  @ToString.Exclude
  @OneToOne(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
  orphanRemoval = true)
  private Cart cart;
}

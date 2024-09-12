package com.ecommerce.project.security.services;

import com.ecommerce.project.model.Users;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@NoArgsConstructor
@Data
public class UserDetailsImplementation implements UserDetails {

  /*
     used in classes that implement the Serializable interface. Its purpose is to ensure that during deserialization
     (the process of converting a byte stream back into an object), the class matches the serialized object (structure of the
     class—its fields, methods, and other components).
   */
  @Serial
  private static final long serialVersionUID = 1L;

  private Long id;
  private String username;
  private String email;

  /*
     Annotation will ignore the field during serialization into JSON.
   */
  @JsonIgnore
  private String password;

  /*
     Collection <? extends GrantedAuthority> means it's a collection that can hold any type of object that is a
     GrantedAuthority or a subclass of it.
   */
  private Collection<? extends GrantedAuthority> authorities;

  public UserDetailsImplementation(Long id, String username, String email, String password,
                                   Collection<? extends GrantedAuthority> authorities){
    this.id = id;
    this.username = username;
    this.email = email;
    this.password = password;
    this.authorities = authorities;
  }

  /*
      The method can be called without creating an instance of the UserDetailsImplementation class. It's a utility method.
   */
  public static UserDetailsImplementation build(Users users){

    /*
      stream():
          Converts the list of roles into a stream so that operations can be performed on each role.

      map(roles -> new SimpleGrantedAuthority(roles.getRoleName().name())):
          This line transforms each Role object into a SimpleGrantedAuthority object, which is a Spring Security class that
          represents an authority (like a role or permission). It takes the role name (e.g., "ADMIN") and wraps it into a
          SimpleGrantedAuthority object.

      collect(Collectors.toList()):
          Finally, the stream of SimpleGrantedAuthority objects is collected into a List.

      Example:
          Let's say the users object has two roles: "ADMIN" and "USER."

          users.getRolesInUsers(): Returns a list of roles: ["ADMIN", "USER"].
          stream(): Converts the list into a stream for processing.
          map(): Transforms each role:
          "ADMIN" becomes new SimpleGrantedAuthority("ADMIN")
          "USER" becomes new SimpleGrantedAuthority("USER")
          collect(Collectors.toList()): Collects these into a list.


      Final Result:
          The authoritiesList will contain two SimpleGrantedAuthority objects:

          SimpleGrantedAuthority("ADMIN")
          SimpleGrantedAuthority("USER")
     */
    List<GrantedAuthority> authoritiesList = users.getRolesInUsers().stream()
      .map(roles ->
        /*
          Example:
              enum RoleName {
                  ADMIN,
                  USER
              }

          roles.getRoleName():
              returns RoleName.ADMIN

          name():
             returns "ADMIN"
             Returns the name of this enum constant, exactly as declared in its enum declaration.

         */

        /*
          SimpleGrantedAuthority:
             It stores a single role or permission (authority) as a string, such as "ROLE_ADMIN" or "ROLE_USER".
         */
        new SimpleGrantedAuthority(roles.getRoleName().name()))
      .collect(Collectors.toList());

      /*
      Resulting UserDetailsImplementation Object:
        - id: 1L
        - username: "john_doe"
        - email: "john@example.com"
        - password: "password123"
        - authorities: [ROLE_USER, ROLE_ADMIN]  // List of GrantedAuthority objects
     */
    return new UserDetailsImplementation(
      users.getId(),
      users.getUserName(),
      users.getEmail(),
      users.getPassword(),
      authoritiesList
    );
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return username;
  }

  /*
  public Long getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }
  */

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public boolean equals(Object objects){
    if (this == objects){
      return true;
    }
    if(objects == null || getClass()!=objects.getClass()){
      return false;
    }

    UserDetailsImplementation user = (UserDetailsImplementation) objects;
    return Objects.equals(id, user.id);
  }
}

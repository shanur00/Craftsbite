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


  @Serial
  private static final long serialVersionUID = 1L;

  private Long id;
  private String username;
  private String email;

  @JsonIgnore
  private String password;

  private Collection<? extends GrantedAuthority> authorities;

  public UserDetailsImplementation(Long id, String username, String email, String password,
                                   Collection<? extends GrantedAuthority> authorities){
    this.id = id;
    this.username = username;
    this.email = email;
    this.password = password;
    this.authorities = authorities;
  }

  public static UserDetailsImplementation build(Users users){

    List<GrantedAuthority> authoritiesList = users.getRolesInUsers().stream()
      .map(roles ->
        new SimpleGrantedAuthority(roles.getRoleName().name()))
      .collect(Collectors.toList());


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

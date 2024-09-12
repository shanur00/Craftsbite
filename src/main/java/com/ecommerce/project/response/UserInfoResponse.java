package com.ecommerce.project.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class UserInfoResponse {
  private Long id;
  private String username;
  private String jwtToken;
  private List<String> roles;

  public UserInfoResponse(Long id, String username, List<String> roles, String jwtToken) {
    this.id = id;
    this.username = username;
    this.roles = roles;
    this.jwtToken = jwtToken;
  }

  public UserInfoResponse(Long id, String username, List<String> roles) {
    this.id = id;
    this.username = username;
    this.roles = roles;
  }
}

package com.ecommerce.project.utils;

import com.ecommerce.project.model.Users;
import com.ecommerce.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class AuthUtil {

  @Autowired
  UserRepository userRepository;


  public String loggedInEmail() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    Users user = userRepository.findByUserName(authentication.getName())
      .orElseThrow(()->new UsernameNotFoundException("User Not Found"));

    return user.getEmail();
  }

  public Long loggedInUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    Users user = userRepository.findByUserName(authentication.getName())
      .orElseThrow(()-> new UsernameNotFoundException("User Not Found"));

    return user.getId();
  }

  public Users loggedInUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    return userRepository.findByUserName(authentication.getName())
      .orElseThrow(()-> new UsernameNotFoundException("User Not Found"));
  }
}

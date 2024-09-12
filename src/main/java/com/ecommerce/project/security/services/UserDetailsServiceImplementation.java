package com.ecommerce.project.security.services;

import com.ecommerce.project.model.Users;
import com.ecommerce.project.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImplementation implements UserDetailsService {

  @Autowired
  UserRepository userRepository;

  @Override
  /*
    It ensures that a method runs within a database transaction. If anything goes wrong (like an exception),
    it rolls back the transaction, so the database remains consistent.
   */
  @Transactional
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Users users = userRepository.findByUserName(username)
      .orElseThrow(()->new UsernameNotFoundException("Username Not Found!"));

    return UserDetailsImplementation.build(users);
  }
}
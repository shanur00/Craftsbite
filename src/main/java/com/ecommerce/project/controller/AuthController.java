package com.ecommerce.project.controller;

import com.ecommerce.project.model.AppRole;
import com.ecommerce.project.model.Roles;
import com.ecommerce.project.model.Users;
import com.ecommerce.project.repository.RoleRepository;
import com.ecommerce.project.repository.UserRepository;
import com.ecommerce.project.request.LoginRequest;
import com.ecommerce.project.request.SignupRequest;
import com.ecommerce.project.response.MessageResponse;
import com.ecommerce.project.response.UserInfoResponse;
import com.ecommerce.project.security.jwt.JwtUtils;
import com.ecommerce.project.security.services.UserDetailsImplementation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  @Autowired
  private JwtUtils jwtUtils;

  @Autowired
  private AuthenticationManager authenticationManager;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private RoleRepository roleRepository;

  @PostMapping("/signin")
  public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest){
    Authentication authentication;
    try {
      authentication = authenticationManager
        .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
    } catch (AuthenticationException exception){
      Map<String, Object> map = new HashMap<>();
      map.put("message", "Bad Credentials");
      map.put("status", false);
      return new ResponseEntity<Object>(map, HttpStatus.NOT_FOUND);
    }

    SecurityContextHolder.getContext().setAuthentication(authentication);

    /*
        Using Our Custom UserdetailsImplementation.
    */
    UserDetailsImplementation userDetails = (UserDetailsImplementation) authentication.getPrincipal();

    ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

    List<String> roles = userDetails.getAuthorities().stream()
      .map(GrantedAuthority::getAuthority)
      .collect(Collectors.toList());

    /*
        Here We just want to get the details of the user who logs in. So we need to fetch the userId too.
    */
    UserInfoResponse loginResponse = new UserInfoResponse(userDetails.getId(), userDetails.getUsername(), roles);

    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString()).body(loginResponse);
  }

  @PostMapping("/signup")
  /*SignupRequest:

      {
        "username": "john_doe",
        "email": "john.doe@example.com",
        "password": "password123",
        "role": ["user", "admin"]
      }
   */
  public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest){
    if(userRepository.existsByUserName(signupRequest.getUsername())){
      return ResponseEntity.badRequest().body("Username is already in use");
    }

    if(userRepository.existsByEmail(signupRequest.getEmail())){
      return ResponseEntity.badRequest().body("Email is already in use");
    }

    Users users = new Users(signupRequest.getUsername(), signupRequest.getEmail(),
      passwordEncoder.encode(signupRequest.getPassword()));

    /*
      Set<String> stringRole:
         Extracts the roles from the SignupRequest. This can be roles like "admin", "user", or "seller" (optional field).
     */
    Set<String> stringRole = signupRequest.getRole();

    /*
      Set<Roles> roles = new HashSet<>():
         Prepares a new set of Roles objects that will later be assigned to the user.
     */
    Set<Roles> roles = new HashSet<>();

    if(stringRole == null){
      /*
        The orElseThrow() method is part of the Optional class which in the RoleRepository;

        roleRepository.findByRoleName(AppRole.ROLE_USER):
            This method checks the roles table in the database for a record with the name ROLE_USER.
       */
      Roles role = roleRepository.findByRoleName(AppRole.ROLE_USER).
        orElseThrow(()-> new RuntimeException("Error: Role Not Found!"));

      roles.add(role);
    }

    else{
      stringRole.forEach(role->{
        switch(role){
          case "admin":
            Roles adminRole = roleRepository.findByRoleName(AppRole.ROLE_ADMIN).
              orElseThrow(()-> new RuntimeException("Error: Role Not Found!"));
            roles.add(adminRole);
            break;

          case "seller":
            Roles sellerRole = roleRepository.findByRoleName(AppRole.ROLE_SELLER)
              .orElseThrow(()-> new RuntimeException("Error: Role Not Found!"));
            break;

          default:
            Roles userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
              .orElseThrow(()-> new RuntimeException("Error: Role Not Found!"));
        }
      });
    }

    users.setRolesInUsers(roles);
    userRepository.save(users);

    return ResponseEntity.ok(new MessageResponse("User Registered Successfully!"));
  }

  @GetMapping("/username")
  public String currentUserName(Authentication authentication){
    if(authentication!=null){
      return authentication.getName();
    }
    else{
      return "";
    }
  }

  /*
      The Authentication Interface is representing the user who is right now authenticated in the system.
      The object is automatically passed to the method. And Also automatically being made available to get the details
      from the object.
   */
  @GetMapping("/user")
  public ResponseEntity<?> getUserDetails(Authentication authentication){
    UserDetailsImplementation userDetails = (UserDetailsImplementation) authentication.getPrincipal();
    List<String> roles = userDetails.getAuthorities().stream()
      .map(item->item.getAuthority())
      .collect(Collectors.toList());

    UserInfoResponse response = new UserInfoResponse(userDetails.getId(), userDetails.getUsername(), roles);

    return ResponseEntity.ok().body(response);
  }

  @PostMapping("/signout")
  public ResponseEntity<?> signOutUser(){
    ResponseCookie cookie = jwtUtils.getCleanJwtCookie();

    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(new MessageResponse("You've been signed Out") );
  }
}

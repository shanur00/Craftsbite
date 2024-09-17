package com.ecommerce.project.security;

import com.ecommerce.project.model.AppRole;
import com.ecommerce.project.model.Roles;
import com.ecommerce.project.model.Users;
import com.ecommerce.project.repository.RoleRepository;
import com.ecommerce.project.repository.UserRepository;
import com.ecommerce.project.security.jwt.AuthEntryPointJwt;
import com.ecommerce.project.security.jwt.AuthTokenFilter;
import com.ecommerce.project.security.services.UserDetailsServiceImplementation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.sql.DataSource;
import java.util.Set;

@Configuration
@EnableWebSecurity
//@EnableMethodSecurity
public class WebSecurityConfig {

  @Autowired
  UserDetailsServiceImplementation userDetailsServiceImplementation;

  @Autowired
  AuthEntryPointJwt unauthorizedHandler;

  @Bean
  public AuthTokenFilter authenticationJwtTokenFilter(){
    return new AuthTokenFilter();
  }
  @Bean
  public DaoAuthenticationProvider authenticationProvider(){
    DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();

    authenticationProvider.setUserDetailsService(userDetailsServiceImplementation);

    authenticationProvider.setPasswordEncoder(passwordEncoder());

    return authenticationProvider;
  }


  @Bean
  public SecurityFilterChain filterChain (HttpSecurity http) throws Exception{
    http.csrf(AbstractHttpConfigurer::disable)

      .exceptionHandling(exception-> exception.authenticationEntryPoint(
        unauthorizedHandler))
      .sessionManagement(session -> session.sessionCreationPolicy(
        SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(auth ->
          auth.requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/v3/api-docs/**").permitAll()
            .requestMatchers("/h2-console/**").permitAll()
            //.requestMatchers("/api/admin/**").permitAll()
            //.requestMatchers("/api/public/**").permitAll()
            .requestMatchers("/swagger-ui/**").permitAll()
            .requestMatchers("/api/test/**").permitAll()
            .requestMatchers("/images/**").permitAll()
            .anyRequest().authenticated()
        );

    http.authenticationProvider(authenticationProvider());

    http.addFilterBefore(authenticationJwtTokenFilter(),
      UsernamePasswordAuthenticationFilter.class);

    http.headers(headers-> headers.frameOptions(
      HeadersConfigurer.FrameOptionsConfig::sameOrigin
    ));

    return http.build();
  }

  @Bean
  public WebSecurityCustomizer webSecurityCustomizer(){
    return (web -> web.ignoring().requestMatchers(
      "/v2/api-docs",
      "/configuration-ui",
      "/swagger-resources",
      "/configuration/security",
      "/swagger-ui.html",
      "/webjars/**"
    ));
  }


  @Bean
  public CommandLineRunner initData(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
    return args -> {
      Roles userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
        .orElseGet(() -> {
          Roles newUserRole = new Roles(AppRole.ROLE_USER);
          return roleRepository.save(newUserRole);
        });

      Roles sellerRole = roleRepository.findByRoleName(AppRole.ROLE_SELLER)
        .orElseGet(() -> {
          Roles newSellerRole = new Roles(AppRole.ROLE_SELLER);
          return roleRepository.save(newSellerRole);
        });

      Roles adminRole = roleRepository.findByRoleName(AppRole.ROLE_ADMIN)
        .orElseGet(() -> {
          Roles newAdminRole = new Roles(AppRole.ROLE_ADMIN);
          return roleRepository.save(newAdminRole);
        });

      Set<Roles> userRoles = Set.of(userRole);
      Set<Roles> sellerRoles = Set.of(sellerRole);
      Set<Roles> adminRoles = Set.of(userRole, sellerRole, adminRole);


      // Create users if not already present
      if (!userRepository.existsByUserName("user1")) {
        Users user1 = new Users("user1", "user1@example.com", passwordEncoder.encode("password1"));
        userRepository.save(user1);
      }

      if (!userRepository.existsByUserName("seller1")) {
        Users seller1 = new Users("seller1", "seller1@example.com", passwordEncoder.encode("password2"));
        userRepository.save(seller1);
      }

      if (!userRepository.existsByUserName("admin")) {
        Users admin = new Users("admin", "admin@example.com", passwordEncoder.encode("adminPass"));
        userRepository.save(admin);
      }

      // Update roles for existing users
      userRepository.findByUserName("user1").ifPresent(user -> {
        user.setRolesInUsers(userRoles);
        userRepository.save(user);
      });

      userRepository.findByUserName("seller1").ifPresent(seller -> {
        seller.setRolesInUsers(sellerRoles);
        userRepository.save(seller);
      });

      userRepository.findByUserName("admin").ifPresent(admin -> {
        admin.setRolesInUsers(adminRoles);
        userRepository.save(admin);
      });
    };
  }

  @Bean
  public PasswordEncoder passwordEncoder(){
    return new BCryptPasswordEncoder();
  }

  @Bean
  AuthenticationManager authenticationManager(AuthenticationConfiguration builder) throws Exception{
    return builder.getAuthenticationManager();
  }
}

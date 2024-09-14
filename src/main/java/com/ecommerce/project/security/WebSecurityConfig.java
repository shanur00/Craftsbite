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

  /*
      DaoAuthenticationProvider is a built-in class in Spring Security that is used to authenticate users by retrieving user
      information from a database or any other persistent storage via a UserDetailsService.

      It delegates the retrieval of user details (like username, password, and roles) to a UserDetailsService implementation
      and handles password matching using a PasswordEncoder.

      This class is essential when you are working with authentication that relies on persistent user data, such as in
      database-backed user authentication.
   */
  @Bean
  public DaoAuthenticationProvider authenticationProvider(){
    DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();

    /*
      The UserDetailsService is an interface in Spring Security used to fetch user details from a persistent store
      (like a database). The method setUserDetailsService assigns an implementation of UserDetailsService that will be
      used by DaoAuthenticationProvider to load the user's information.
     */
    authenticationProvider.setUserDetailsService(userDetailsServiceImplementation);

    /*
      The `PasswordEncoder` is used to encode (hash) the password when the user registers, and it is also used to match the
      encoded password when the user tries to log in. Spring Security uses this encoder to ensure passwords are stored
      securely in hashed form.

      In your method, the `passwordEncoder()` is likely a separate method that returns an instance of a `PasswordEncoder`,
      such as `BCryptPasswordEncoder`.
     */
    authenticationProvider.setPasswordEncoder(passwordEncoder());

    return authenticationProvider;
  }


  /*
      HttpSecurity is a class in Spring Security that allows you to configure the security for HTTP requests in a web
      application. It provides a set of methods to customize how your application handles security concerns like authentication,
      authorization, CSRF protection, session management, and more.

      SecurityFilterChain uses the HttpSecurity class to define how HTTP requests are handled, including authentication and authorization rules, filter chains, session
      management, and CSRF protection.
   */
  @Bean
  public SecurityFilterChain filterChain (HttpSecurity http) throws Exception{
    http.csrf(AbstractHttpConfigurer::disable)
      /*
        AuthenticationEntryPoint is the Interface and you implement it in AuthEntryPointJWT. Now To handle exception the
        authenticationEntryPoint will be called and an instance of the AuthenticationEntryPoint should be passed.

          public ExceptionHandlingConfigurer<H> authenticationEntryPoint(AuthenticationEntryPoint authenticationEntryPoint) {
              this.authenticationEntryPoint = authenticationEntryPoint;
              return this;
        	}
       */
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

    /*
      This ensures that the authentication process uses your custom authenticationProvider, which could retrieve user details
      from a database and match them against provided credentials using a password encoder (like BCrypt).
     */
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

  /*@Bean
  SecurityFilterChain defaultSecurityFilterChain(HttpSecurity httpSecurity) throws Exception{
    httpSecurity.authorizeHttpRequests((request)->
      request.requestMatchers("/h2-console/**", "/api/signin").permitAll()
        .anyRequest().authenticated()
    );

    httpSecurity.sessionManagement(session->
      session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    );

    httpSecurity.exceptionHandling(exception-> exception.authenticationEntryPoint(unauthorizedHandler));

    httpSecurity.httpBasic(Customizer.withDefaults());

    httpSecurity.headers(headers->
      headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
    );

    httpSecurity.csrf(AbstractHttpConfigurer::disable);

    httpSecurity.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

    return httpSecurity.build();
  }*/

 /* @Bean
  public UserDetailsService userDetailsService(){
    return new JdbcUserDetailsManager(dataSource);
  }
  */

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

  /*
     Spring automatically sets up the AuthenticationManager based on the security configuration
     (SecurityFilterChain) you provide (which includes the
     UserDetailsService, password encoder, and any custom authentication logic).
   */
  @Bean
  AuthenticationManager authenticationManager(AuthenticationConfiguration builder) throws Exception{
    return builder.getAuthenticationManager();
  }
}

package com.ecommerce.project.security.jwt;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
/*
  OncePerRequestFilter is a base class provided by Spring Security that ensures the filter is executed only
  once per request.

  By extending OncePerRequestFilter, the AuthTokenFilter will be applied to every incoming request but will
  run only once for each request.
 */
public class AuthTokenFilter extends OncePerRequestFilter {
  @Autowired
  private JwtUtils jwtUtils;

  @Autowired
   /*
    This is an interface provided by Spring Security. It has a method called loadUserByUsername
    (String username) that is used to fetch the user details object (like username, password, roles, etc.)
    from a data source (e.g., database) based on the username. The fetched UserDetails object will then be
    used to authenticate the user.

    The implementation of UserDetailsService typically retrieves a UserDetails object, which contains the
    user's credentials and authorities.
   */
  private UserDetailsService userDetailsService;

  private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  /*
                                    All parameters should be non-null unless explicitly stated otherwise.
                                    This rule is enforced by an annotation called @NonNullApi, which is applied at
                                    the package or class level. It means that any method parameter within this class or
                                    package is expected to be non-null by default, unless you mark
                                    it as @Nullable (i.e., it can be null).

                                    Now, when you override a method from that superclass, you also need to follow this rule.
                                    If you don't explicitly mark the parameters as @NonNull, you might get a warning or error
                                    saying that your method parameters do not follow the non-null contract.
                                   */
                                  @Nonnull HttpServletResponse response, @Nonnull FilterChain filterChain)
    throws ServletException, IOException {
    logger.debug("AuthTokenFilter Called for the URI: {}", request.getRequestURI());

    try {
       /*
        Assuming the JWT is "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huZG9lIiwianRpIjoiMTIzNDU2Nzg5MCIsImlhdCI6MTUxNjIzOT
        AyMn0.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
        the jwt variable will hold this token string.
       */
      String jwtToken = parseJwtToken(request);
      if(jwtToken!=null && jwtUtils.validateJwtToken(jwtToken)){
        String username = jwtUtils.getUsernameFromJwtToken(jwtToken);

        /*
          Loads user details using the UserDetailsService by the extracted username.
          If username is "johndoe", userDetails might include information like the username, password, and
          authorities (e.g., ROLE_USER).
         */
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

         /*
          UsernamePasswordAuthenticationToken:
               This is a class provided by Spring Security.
               It is a specific implementation of the Authentication interface.
               It is used to store the authentication information, including the user's credentials
               (like username and password) and their roles or authorities.

          userDetails:
               This object is an instance of the UserDetails interface.
               It contains information about the user, such as their username, password (encrypted),
               and granted authorities (roles).
               userDetails would typically be loaded from a database or other data source by a UserDetailsService
               implementation.

          null (as the second argument):
               The second argument in the constructor represents the credentials (usually a password) of the user.
               In this case, it is set to null because the credentials are not needed after authentication has already
               been performed (for example, after extracting and verifying a JWT token).

          userDetails.getAuthorities():
               This method returns a collection of GrantedAuthority objects.
               GrantedAuthority represents an authority granted to the user, typically a role (e.g., ROLE_USER, ROLE_ADMIN).
               These authorities are used by Spring Security to manage access control in the application.

          In Spring Security, the Principal object is often an instance of org.springframework.security.core.userdetails.User
          or a custom implementation of UserDetails.

          After the user has been authenticated, there is no longer a need to store the user's credentials
          (like their password) in memory. To avoid security risks, such as exposing sensitive information,
          Spring Security sets the credentials field to null.

          This method collect all Roles in UserDetails Class
          Collection<? extends GrantedAuthority> getAuthorities();
         */
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails,
          null, userDetails.getAuthorities());

        logger.debug("Roles from JWT: {}", userDetails.getAuthorities());

         /*
          Adds additional details to the authentication token, such as the IP address or session ID.
          authenticationToken Object:
               Principal (User): johndoe
               Credentials (Password): null
               Authorities (Roles): [ROLE_USER, ROLE_ADMIN]
               Details: The WebAuthenticationDetails object containing:
                         Remote IP Address: 192.168.1.10
                         Session ID: ABCD1234

           public WebAuthenticationDetails buildDetails(HttpServletRequest context) {
             return new WebAuthenticationDetails(context);
            }

          This creates an instance of WebAuthenticationDetails, which contains details about the web request,
          like the user's IP address and session ID.

         */
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        /*
          By setting the authenticationToken into the SecurityContext, you’re telling Spring Security that the current
          user is authenticated. From this point onward, the user’s identity and roles are available throughout the
          application for authorization checks.

          SecurityContextHolder:
               A "holder" or a container that gives you a way to get or set the SecurityContext for the
               current thread.  Imagine it as a safe that stores the security context (user’s security details).

          SecurityContext:
               The SecurityContext is an Interface holds the authentication information, such as the authenticated user’s details,
               credentials, and granted authorities (roles/permissions).  This is the actual content inside the
               safe—specifically, the information about the currently authenticated user.

          public static SecurityContext getContext() {
             return strategy.getContext();
          }
         */
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
      }
    } catch (Exception exception){
      logger.error("Can't Set User Authentication: ", exception);
    }

      /*
       This method passes the HttpServletRequest and HttpServletResponse to the next filter in the chain or to the
       servlet if there are no more filters.

      What Actually Happens:
           Filter A Starts Processing:
                   When the request hits Filter A, its doFilterInternal method is called.

           Filter A Calls filterChain.doFilter:
                   Filter A does its job and then calls filterChain.doFilter(request, response). This passes control
                   to Filter B, the next filter in the chain.

           Filter B Starts Processing:
                   Filter B's doFilterInternal method gets called. Filter B does its processing and again calls
                   filterChain.doFilter(request, response).

           Final Destination:
                   Eventually, if there are no more filters, the request reaches its final destination
                   (like a servlet).
     */
    filterChain.doFilter(request, response);
  }

  private String parseJwtToken(HttpServletRequest request){
    String jwt = jwtUtils.getJwtFromHeader(request);
    logger.debug("AuthTokenFilter.java: {}", jwt);
    return jwt;
  }
}

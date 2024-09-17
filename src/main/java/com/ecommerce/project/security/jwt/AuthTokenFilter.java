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
public class AuthTokenFilter extends OncePerRequestFilter {
  @Autowired
  private JwtUtils jwtUtils;

  @Autowired

  private UserDetailsService userDetailsService;

  private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  @Nonnull HttpServletResponse response, @Nonnull FilterChain filterChain)
    throws ServletException, IOException {
    logger.debug("AuthTokenFilter Called for the URI: {}", request.getRequestURI());

    try {
      String jwtToken = parseJwtToken(request);
      if(jwtToken!=null && jwtUtils.validateJwtToken(jwtToken)){
        String username = jwtUtils.getUsernameFromJwtToken(jwtToken);

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);


        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails,
          null, userDetails.getAuthorities());

        logger.debug("Roles from JWT: {}", userDetails.getAuthorities());

        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
      }
    } catch (Exception exception){
      logger.error("Can't Set User Authentication: ", exception);
    }

    filterChain.doFilter(request, response);
  }

  private String parseJwtToken(HttpServletRequest request){
    String jwt = jwtUtils.getJwtFromCookies(request);
    logger.debug("AuthTokenFilter.java: {}", jwt);
    return jwt;
  }
}

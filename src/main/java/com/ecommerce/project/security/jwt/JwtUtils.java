package com.ecommerce.project.security.jwt;

import com.ecommerce.project.security.services.UserDetailsImplementation;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

  private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

  @Value("${spring.app.jwtSecret}")
  private String jwtSecret;

  @Value("${spring.app.jwtExpirationMs}")
  private int jwtExpirationMs;

  @Value("${spring.app.jwtCookie}")
  private String jwtCookie;

  public String getJwtFromHeader(HttpServletRequest request){
    String bearToken = request.getHeader("Authentication");
    logger.debug("Authorization Header {}: ", bearToken);

    if(bearToken!=null && bearToken.startsWith("Bearer ")){
      return bearToken.substring(7);
    }
    return null;
  }


  public String getJwtFromCookies(HttpServletRequest request){
    Cookie cookie = WebUtils.getCookie(request, jwtCookie);
    if(cookie!=null){
      System.out.println("Cookie: "+cookie);
      return cookie.getValue();
    }
    else{
      return null;
    }
  }

  public ResponseCookie generateJwtCookie(UserDetailsImplementation userPrinciple){
    String jwt = generateTokenFromUsername(userPrinciple.getUsername());
    return ResponseCookie.from(jwtCookie, jwt).path("/api").
      maxAge(24*60*60)
      .httpOnly(false)
      .build();
  }

  public ResponseCookie getCleanJwtCookie(){
    return ResponseCookie.from(jwtCookie, null)
      .path("/api")
      .build();
  }

  public String generateTokenFromUsername(String username) {

    return Jwts.builder()
      .subject(username)
      .issuedAt(new Date())
      .expiration(new Date(new Date().getTime()+jwtExpirationMs))
      .signWith(key())
      .compact();
  }

  private Key key() {
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
  }

  public String getUsernameFromJwtToken(String token){
    return Jwts.parser()
      .verifyWith((SecretKey) key())
      .build()
      .parseSignedClaims(token)
      .getPayload()
      .getSubject();
  }

  public boolean validateJwtToken(String authToken){
    try {
      System.out.println("Validate");
      Jwts.parser()
        .verifyWith((SecretKey) key())
        .build()
        .parseSignedClaims(authToken);
      return true;
    } catch (MalformedJwtException malformedJwtException){
      logger.error("Invalid JWT Token: {}", malformedJwtException.getMessage());
    } catch (ExpiredJwtException expiredJwtException){
      logger.error("JWT Token is Expired: {}", expiredJwtException.getMessage());
    } catch (UnsupportedJwtException unsupportedJwtException){
      logger.error("JWT Token is Unsupported: {}", unsupportedJwtException.getMessage());
    } catch (IllegalArgumentException illegalArgumentException){
      logger.error("JWT claim String is Empty: {}", illegalArgumentException.getMessage());
    }
    return false;
  }
}

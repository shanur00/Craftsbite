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
  /*
     This line creates a logger instance for logging messages. It's useful for tracking events, debugging, and
     recording errors in the application. LoggerFactory is part of the SLF4J (Simple Logging Facade for Java) library.
     This is a factory method that creates the logger specifically for the JwtUtils class.
     It means that any log message generated will be associated with this class.

     LoggerFactory.getLogger(JwtUtils.class):
          This is a factory method that creates the logger specifically for the
          JwtUtils class. It means that any log message generated will be associated with this class.
   */
  private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

  /*
     These fields are injected with values from the application's properties file (application.properties).
     -- jwtSecret: The secret key used for signing JWT tokens.
     -- jwtExpirationMs: The expiration time for the JWT token in milliseconds.
   */
  @Value("${spring.app.jwtSecret}")
  private String jwtSecret;

  @Value("${spring.app.jwtExpirationMs}")
  private int jwtExpirationMs;

  @Value("${spring.app.jwtCookie}")
  private String jwtCookie;

  /*
     This method extracts the JWT from the Authorization header of an HTTP request.
     -- Retrieves the Authorization header value.
     -- Logs the Authorization header for debugging.
     -- Checks if the header starts with " Bearer ".
     -- If it does, it returns the JWT by removing the "Bearer " prefix.
     -- If the header is absent or doesn't start with "Bearer ", it returns null.
     Example:
      GET /api/user/profile HTTP/1.1
      Host: www.example.com
      Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huZG9lIiwiaWF0IjoxNjE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
*/
  public String getJwtFromHeader(HttpServletRequest request){
    String bearToken = request.getHeader("Authentication");
    logger.debug("Authorization Header {}: ", bearToken);

    if(bearToken!=null && bearToken.startsWith("Bearer ")){
      return bearToken.substring(7);
    }
    return null;
  }


  /*
      Cookie cookie = WebUtils.getCookie(request, jwtSecret):
           This line tries to find a specific cookie in the incoming HTTP request. The cookie's name is stored in jwtSecret,
           which is assumed to be a string representing the cookie's name (for example, "authToken").

           Method WebUtils.getCookie(request, jwtSecret):
              This method looks through all cookies in the request and returns the one whose name matches jwtSecret.
              If it finds the cookie, it returns it. If not, it returns null.

           Example:

              Suppose the cookie name (jwtSecret) is "authToken", and the request contains this cookie:
                Cookie: authToken=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

      cookie.getValue():
           Extracts the JWT from the cookie.

           If the cookie was found, the console might output something like:
              Cookie: [authToken=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...]

           The value of a cookie is typically the JWT itself, which might look something like this:
              "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

   */
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

    /*
      ResponseCookie.from(jwtCookie, jwt):
         If jwtCookie = "authToken", then this line creates a cookie with the name "authToken" and the value being the JWT, something like:
             "authToken=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."


      .path("/api"):
         This sets the URL path where the cookie is available. The /api path means the cookie will only be sent to the server when
         the user accesses the /api path (or its sub-paths).
            Example:
                If the user accesses https://example.com/api/user, this cookie will be sent along with the request.
                If the user accesses https://example.com/home, the cookie will not be sent.


      .httpOnly(false):
         This controls whether the cookie is accessible via client-side JavaScript.
             Example:
              If you set httpOnly(true), the cookie can only be sent via HTTP requests and won't be accessible from JavaScript
              (making it more secure).
              Here, httpOnly(false) allows the client-side code (JavaScript) to read and use the cookie.

      .build():
          This finalizes the cookie building process and returns a ResponseCookie object ready to be added to the HTTP response.
             Example:
                 {
                    "name": "authToken",
                    "value": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                    "path": "/api",
                    "maxAge": 86400,
                    "httpOnly": false
                 }

    */

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

  /*
     This method generates a JWT based on the user's username.
     -- Retrieves the username from UserDetails.
     -- Creates a JWT using the Jwts.builder():
        setSubject(username): Sets the username as the subject (the main content of the token).
        setIssuedAt(new Date()): Sets the issue date to the current time.
        setExpiration(...): Sets the expiration time based on the current time plus jwtExpirationMs.
        signWith(key()): Signs the token with the secret key generated by the key() method.
     Example:
      Username: johndoe
      Password: password123
      jwtExpirationMs (token expiration time) is set to 1 hour (3600000 milliseconds).
   */
  public String generateTokenFromUsername(String username) {
    /*
       username will be "johndoe".

     */
    //String username = userDetails.getUsername();

    return Jwts.builder()
      /*
         {
           "sub": "johndoe"
         }
       */
      .subject(username)
      /*
         {
           "sub": "johndoe",
           "iat": 1724596200000  // This is the timestamp corresponding to the issued time in milliseconds.
         }
       */
      .issuedAt(new Date())
      /*
         {
           "sub": "johndoe",
           "iat": 1724596200000,  // Issued at time
           "exp": 1724599800000   // Expiration time (1 hour later)
         }
       */
      .expiration(new Date(new Date().getTime()+jwtExpirationMs))
      /*
         {
           "header": {
             "alg": "HS256"
           },
           "payload": {
             "sub": "johndoe",
             "iat": 1724596200000,
             "exp": 1724599800000
           },
           "signature": "exampleSignature"
         }
       */
      .signWith(key())

      /*
         The compact() method finalizes the JWT by combining the header, payload, and signature into a single string in
         the format header.payload.signature.
         "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huZG9lIiwiaWF0IjoxNjM5Mzg1NjAwLCJleHAiOjE2MzkzODU2MDB9.exampleSignature"
       */
      .compact();
  }

  /*
     jwtSecret is "c2VjcmV0S2V5MTIzIQ==", which is the Base64 encoding of "secretKey123!".
     The decoded byte array would represent "secretKey123!".
     The hmacShaKeyFor() method from the Keys class uses this byte array to generate a Key object that is compatible
     with HMAC SHA algorithms.

     Imagine you want to protect a message with a secret code. You use a codebook (your jwtSecret).
     Before using it, you convert the codebook from a special format (Base64) into a readable format.
     Then, you use this codebook to create a special stamp (HMAC) that you place on the message (JWT).

     The key() method will produce the same Key object every time it is called with the same jwtSecret.
   */
  private Key key() {
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
  }

  /*
     JWT token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huZG9lIiwianRpIjoiMTIzNDU2Nzg5MCIsImlhdCI6MTUxNjIzOTAyMn0.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
     Jwts.parser(): This creates a new parser instance for parsing the JWT token. No immediate output,
        just a JwtParser object being prepared for further processing.

     verifyWith((SecretKey) key()): Configures the parser with the secret key needed to verify the JWT's signature.

     build(): Finalizes the configuration of the parser, making it ready to parse and validate the token.
         Output a fully configured JwtParser object.

     parseSignedClaims(token):  Parses the JWT, verifying its signature against the provided secret key,
     and extracts the claims (payload) from the token.

     getPayload(): {
                    "sub": "johndoe",
                    "jti": "1234567890",
                    "iat": 1516239022
                   }

     getSubject(): sub: "johndoe"
   */
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

package com.ecommerce.project.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
/*
   The AuthenticationEntryPoint interface is used in Spring Security to handle what happens when an unauthenticated user
   tries to access a protected resource.

   The AuthenticationEntryPoint (like AuthEntryPointJwt in your context) is called automatically by Spring Security
   when an authentication failure occurs, particularly when an unauthenticated user tries to access a secured resource.
 */
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

  private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
    throws IOException, ServletException {
    logger.error("Unauthorized Error: {}", authException.getMessage());
    logger.error("e: ", authException);

    /*
       Sets the response content type to application/json. This tells the client that the response will be in
       JSON format.

       Example:
       <JSON FORMAT>

        {
          "status": 401,
          "error": "Unauthorized",
          "message": "Authentication is required to access this resource.",
          "path": "/api/protected-endpoint"
        }
     */
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

     /*
      Sets the HTTP status code to 401 Unauthorized, indicating that the user is not authorized to access the
      requested resource.
     */
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

     /*
      This block creates a Map that represents the JSON body of the response.
      body.put(...): Adds key-value pairs to the map that will be included in the JSON response:
                     "status": The HTTP status code (401).
                     "error": A short description of the error (“Unauthorized”).
                     "message": The error message from the AuthenticationException, e.g., “Bad credentials”.
                     "path": The URI path that the user was trying to access, obtained from request.getServletPath().

      Response Body (JSON):
          {
             "status": 401,
             "error": "Unauthorized",
             "message": "Bad credentials",
             "path": "/api/protected-resource"
          }

      Using Object as the value type in the map provides flexibility because it can store any type of value.

     */
    final Map<String, Object> body = new HashMap<>();
    body.put("status",HttpServletResponse.SC_UNAUTHORIZED);
    body.put("error", "Unauthorized");
    body.put("message", authException.getMessage());
    body.put("path",request.getServletPath());

    /*
      Creates an instance of ObjectMapper, a class from the Jackson library used for converting Java objects to JSON.

      Example:
            JSON String: {"username":"john_doe","firstName":"John","lastName":"Doe"}
            User Object: User{username='john_doe', firstName='John', lastName='Doe'}
     */
    final ObjectMapper mapper = new ObjectMapper();

      /*
      mapper.writeValue(response.getOutputStream(), body):
            Uses ObjectMapper to serialize a Java object (body) into JSON and write it directly to the HTTP response's
            output stream.


      The JSON response sent to the client will look like this:
            Response Body (JSON):
                {
                  "status": 401,
                  "error": "Unauthorized",
                  "message": "Bad credentials",
                  "path": "/api/protected-resource"
                }
     */
    mapper.writeValue(response.getOutputStream(), body);
  }
}

package com.ecommerce.project.exception;

import com.ecommerce.project.payload.APIResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice

public class MyGlobalExceptionHandler {
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> myMethodArgumentNotValidException(MethodArgumentNotValidException methodArgumentNotValidException){
    Map<String, String> response = new HashMap<>();
    methodArgumentNotValidException.getBindingResult().getAllErrors().forEach(err -> {
      String fieldName = ((FieldError)err).getField();
      String message = err.getDefaultMessage();
      response.put(fieldName, message);
    });
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<APIResponse> myResourceNotFoundException(ResourceNotFoundException resourceNotFoundException){
    String message = resourceNotFoundException.getMessage();
    APIResponse apiResponse = new APIResponse(message, false);
    return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(APIException.class)
  public ResponseEntity<APIResponse> myAPIException(APIException apiException){
    String message = apiException.getMessage();
    APIResponse apiResponse = new APIResponse(message, false);
    return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(EmptyReturnListException.class)
  public ResponseEntity<APIResponse> myEmptyCategoryListException(EmptyReturnListException emptyCategoryListException){
    String message = emptyCategoryListException.getMessage();
    APIResponse apiResponse = new APIResponse(message, false);
    return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
  }
}

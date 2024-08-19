package com.ecommerce.project.exception;

public class EmptyReturnListException extends RuntimeException{
  public EmptyReturnListException() {

  }

  public EmptyReturnListException(String message) {
    super(message);
  }
}

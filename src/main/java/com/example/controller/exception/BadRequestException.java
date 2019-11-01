package com.example.controller.exception;

public class BadRequestException extends RuntimeException{
  public BadRequestException(String msg){
    super(msg);
  }
}

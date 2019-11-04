package com.example.service.exception;


public class AlreadyBookedException extends RuntimeException {

  public AlreadyBookedException(String msg){
    super(msg);
  }
}

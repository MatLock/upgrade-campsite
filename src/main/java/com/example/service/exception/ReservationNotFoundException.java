package com.example.service.exception;

public class ReservationNotFoundException extends  RuntimeException{

  public ReservationNotFoundException(String msg){
    super(msg);
  }
}

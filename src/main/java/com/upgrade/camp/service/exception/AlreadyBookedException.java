package com.upgrade.camp.service.exception;


public class AlreadyBookedException extends RuntimeException {

  public AlreadyBookedException(String msg){
    super(msg);
  }
}

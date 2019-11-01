package com.example.controller.advice;

import com.example.controller.exception.BadRequestException;
import com.example.response.ReservationResponse;
import com.example.service.exception.ModelConstraintReservation;
import com.example.service.exception.ReservationNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ExceptionHandlerController extends ResponseEntityExceptionHandler{

  @ExceptionHandler({ReservationNotFoundException.class, BadRequestException.class, ModelConstraintReservation.class})
  public ResponseEntity<ReservationResponse> handleReservationError(Exception e){
    ReservationResponse response = createReservationResponse(e);
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  private ReservationResponse createReservationResponse(Exception e){
    return ReservationResponse.builder()
                              .error(Boolean.TRUE)
                              .msg(e.getMessage())
                              .build();
  }


}

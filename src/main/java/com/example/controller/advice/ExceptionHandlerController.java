package com.example.controller.advice;

import com.example.controller.exception.BadRequestException;
import com.example.controller.response.ReservationResponse;
import com.example.service.exception.AlreadyBookedException;
import com.example.service.exception.ModelConstraintReservation;
import com.example.service.exception.ReservationNotFoundException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ExceptionHandlerController extends ResponseEntityExceptionHandler{

  @ExceptionHandler({BadRequestException.class, ModelConstraintReservation.class, AlreadyBookedException.class})
  public ResponseEntity<ReservationResponse> handleReservationBadRequestError(Exception e){
    ReservationResponse response = createReservationResponse(e.getMessage());
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler({ReservationNotFoundException.class})
  public ResponseEntity<ReservationResponse> handleReservationNotFoundError(Exception e){
    ReservationResponse response = createReservationResponse(e.getMessage());
    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler({ConstraintViolationException.class})
  public ResponseEntity<ReservationResponse> handleConstraintViolationError(Exception e){
    ReservationResponse response = createReservationResponse("Cannot book due conflict with other reservations");
    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
  }


  private ReservationResponse createReservationResponse(String msg){
    return ReservationResponse.builder()
                              .error(Boolean.TRUE)
                              .msg(msg)
                              .build();
  }


}

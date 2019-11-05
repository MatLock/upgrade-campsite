package com.upgrade.camp.advice;

import com.upgrade.camp.controller.advice.ExceptionHandlerController;
import com.upgrade.camp.controller.exception.BadRequestException;
import com.upgrade.camp.controller.response.ReservationResponse;
import com.upgrade.camp.service.exception.ModelConstraintReservation;
import com.upgrade.camp.service.exception.ReservationNotFoundException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static org.hamcrest.CoreMatchers.is;

@RunWith(MockitoJUnitRunner.class)
public class ExceptionHandlerControllerTest {

  private static final String BAD_REQUEST_EX = "bad request exception";
  private static final String RESERVATION_NOT_FOUND_EX = "reservation not found";
  private static final String CONSTRAINT_EX_PRETTY = "Cannot book due conflict with other reservations";

  @Mock
  private BadRequestException badRequestException;
  @Mock
  private ReservationNotFoundException reservationNotFoundException;
  @Mock
  private ConstraintViolationException constraintViolationException;

  @InjectMocks
  private ExceptionHandlerController exceptionHandlerController;

  @Before
  public void setUp(){
    when(badRequestException.getMessage()).thenReturn(BAD_REQUEST_EX);
    when(reservationNotFoundException.getMessage()).thenReturn(RESERVATION_NOT_FOUND_EX);
  }

  @Test
  public void testHandleBadRequest(){
    ResponseEntity<ReservationResponse> response  =exceptionHandlerController.handleReservationBadRequestError(badRequestException);
    assertsOn(response,BAD_REQUEST_EX);
  }

  @Test
  public void testHandleConstraintViolation(){
    ResponseEntity<ReservationResponse> response  = exceptionHandlerController.handleConstraintViolationError(constraintViolationException);
    assertsOn(response,CONSTRAINT_EX_PRETTY);
    verify(constraintViolationException,never()).getMessage();
  }

  @Test
  public void testHandleReservationNotFound(){
    ResponseEntity<ReservationResponse> response  =exceptionHandlerController.handleReservationNotFoundError(reservationNotFoundException);
    assertsOn(response,RESERVATION_NOT_FOUND_EX);
  }

  private void assertsOn(ResponseEntity<ReservationResponse> response,String msg){
    ReservationResponse reservationResponse = response.getBody();
    assertThat(reservationResponse.getError(),is(Boolean.TRUE));
    assertThat(reservationResponse.getResponse(),is(nullValue()));
    assertThat(reservationResponse.getMessage(),is(msg));
  }



}

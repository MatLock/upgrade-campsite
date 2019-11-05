package com.upgrade.camp.unit.test;

import com.upgrade.camp.controller.ReservationController;
import com.upgrade.camp.controller.exception.BadRequestException;
import com.upgrade.camp.controller.request.ReservationRequest;
import com.upgrade.camp.controller.response.AvailableDaysResponse;
import com.upgrade.camp.controller.response.ReservationResponse;
import com.upgrade.camp.model.Reservation;
import com.upgrade.camp.service.ReservationService;
import com.upgrade.camp.service.exception.ModelConstraintReservation;
import com.upgrade.camp.service.exception.ReservationNotFoundException;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ReservationControllerTest {

  private static final String EMAIL = "someEmail@gmail.com";
  private static final String FULL_NAME = "NAME";
  private static final String UID = UUID.randomUUID().toString();
  private static final LocalDateTime START_DATE = LocalDateTime.now();
  private static final LocalDateTime END_DATE = START_DATE.plusDays(3);
  private static final String MODEL_CONSTRAINT_EX = "Camp reservation days cannot be greater than 3 days";
  private static final String NOT_FOUND_EX = "Reservation not found";
  private static final String BAD_REQUEST_EX = "All fields are mandatory";

  @Mock
  private ReservationService reservationService;
  @InjectMocks
  private ReservationController reservationController;
  @Mock
  private ReservationRequest reservationRequest;
  @Mock
  private Reservation reservation;

  private List<LocalDateTime>dates;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setUp(){
    when(reservationRequest.getEmail()).thenReturn(EMAIL);
    when(reservationRequest.getFullName()).thenReturn(FULL_NAME);
    when(reservationRequest.getStartDate()).thenReturn(START_DATE);
    when(reservationRequest.getEndDate()).thenReturn(END_DATE);

    dates = Arrays.asList(LocalDateTime.now());

    doNothing().when(reservationService).createReservation(any(Reservation.class));
    doReturn(reservation).when(reservationService).findById(UID);
    doReturn(reservation).when(reservationService).updateReservation(eq(reservationRequest),eq(UID));
    doReturn(dates).when(reservationService).findAvailability(any(),any());
  }

  @Test
  public void testCreateReservation(){
    ResponseEntity<ReservationResponse> responseEntity = reservationController.saveReservation(reservationRequest);
    ReservationResponse reservationResponse = responseEntity.getBody();
    verify(reservationService).createReservation(any(Reservation.class));
    assertReservationResponseWith(reservationResponse,notNullValue(),nullValue(),Boolean.FALSE);
  }

  @Test
  public void testCreateReservationThrowsException(){
    doThrow(new ModelConstraintReservation(MODEL_CONSTRAINT_EX))
            .when(reservationService).createReservation(any(Reservation.class));
    expectedException.expect(ModelConstraintReservation.class);
    expectedException.expectMessage(MODEL_CONSTRAINT_EX);
    reservationController.saveReservation(reservationRequest);
    verify(reservationService).createReservation(any(Reservation.class));
  }

  @Test
  public void testCreateReservationFailsDueMandatoryFields(){
    doReturn(null).when(reservationRequest).getEmail();
    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage(BAD_REQUEST_EX);
    reservationController.saveReservation(reservationRequest);
    verify(reservationService,never()).createReservation(any(Reservation.class));
  }

  @Test
  public void testFindById(){
    ResponseEntity<ReservationResponse> response = reservationController.findById(UID);
    ReservationResponse reservationResponse = response.getBody();
    assertReservationResponseWith(reservationResponse,reservation,null,Boolean.FALSE);
    verify(reservationService).findById(UID);
  }

  @Test
  public void testFindByIdNotFound(){
    doThrow(new ModelConstraintReservation(NOT_FOUND_EX)).when(reservationService).findById(eq(UID));
    expectedException.expect(ModelConstraintReservation.class);
    expectedException.expectMessage(NOT_FOUND_EX);
    reservationController.findById(UID);
    verify(reservationService).findById(eq(UID));
  }

  @Test
  public void testUpdateReservation(){
    ResponseEntity<ReservationResponse> response = reservationController.updateReservation(reservationRequest,UID);
    ReservationResponse reservationResponse = response.getBody();
    assertReservationResponseWith(reservationResponse,reservation,null,Boolean.FALSE);
    verify(reservationService).updateReservation(eq(reservationRequest),eq(UID));
  }

  @Test
  public void testUpdateNotFound(){
    doThrow(new ReservationNotFoundException(NOT_FOUND_EX)).when(reservationService).updateReservation(eq(reservationRequest),eq(UID));
    expectedException.expect(ReservationNotFoundException.class);
    expectedException.expectMessage(NOT_FOUND_EX);
    reservationController.updateReservation(reservationRequest,UID);
    verify(reservationService).updateReservation(eq(reservationRequest),eq(UID));
  }

  @Test
  public void testDelete(){
    reservationController.deleteReservation(UID,EMAIL);
    verify(reservationService).deleteReservation(eq(UID),eq(EMAIL));
  }

  @Test
  public void testAvailability(){
    ResponseEntity<AvailableDaysResponse> responseEntity = reservationController.checkAvailability(LocalDate.now(),LocalDate.now());
    AvailableDaysResponse response = responseEntity.getBody();
    assertThat(response.getError(),is(Boolean.FALSE));
    assertThat(response.getMessage(),is(nullValue()));
    assertThat(response.getResponse(),equalTo(dates));
  }

  private void assertReservationResponseWith(ReservationResponse reservationResponse, Matcher<Object> response, Matcher<Object> msg, Boolean error){
    assertThat(reservationResponse.getError(),is(error));
    assertThat(reservationResponse.getMessage(),is(msg));
    assertThat(reservationResponse.getResponse(),is(response));
  }

  private void assertReservationResponseWith(ReservationResponse reservationResponse, Reservation response, String msg, Boolean error){
    assertThat(reservationResponse.getError(),is(error));
    assertThat(reservationResponse.getMessage(),is(msg));
    assertThat(reservationResponse.getResponse(),is(response));
  }
}

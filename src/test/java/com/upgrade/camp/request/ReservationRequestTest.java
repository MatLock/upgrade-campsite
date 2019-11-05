package com.upgrade.camp.request;

import com.upgrade.camp.controller.request.ReservationRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

@RunWith(MockitoJUnitRunner.class)
public class ReservationRequestTest {

  private static final String EMAIL = "someEmail@gmail.com";
  private static final String FULL_NAME = "NAME";
  private static final LocalDateTime START_DATE = LocalDateTime.now();
  private static final LocalDateTime END_DATE = START_DATE.plusDays(3);

  private ReservationRequest reservationRequest;

  @Before
  public void setUp(){
    reservationRequest = ReservationRequest.builder()
                                           .email(EMAIL)
                                           .fullName(FULL_NAME)
                                           .startDate(START_DATE)
                                           .endDate(END_DATE)
                                           .build();
  }

  @Test
  public void testAccessors(){
    assertThat(reservationRequest.getEmail(),is(EMAIL));
    assertThat(reservationRequest.getEndDate(),is(END_DATE));
    assertThat(reservationRequest.getFullName(),is(FULL_NAME));
    assertThat(reservationRequest.getStartDate(),is(START_DATE));
  }



}

package com.upgrade.camp.model;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

@RunWith(MockitoJUnitRunner.class)
public class ReservationTest {

  private static final String EMAIL = "someEmail@gmail.com";
  private static final String FULL_NAME = "NAME";
  private static final String UID = UUID.randomUUID().toString();
  private static final LocalDateTime BOOKING_DATE = LocalDateTime.now();
  private static final LocalDateTime START_DATE = LocalDateTime.now();
  private static final LocalDateTime END_DATE = START_DATE.plusDays(3);

  private Reservation reservation;

  @Before
  public void setUp(){
    reservation = Reservation.builder()
                             .bookingDate(BOOKING_DATE)
                             .startDate(START_DATE)
                             .endDate(END_DATE)
                             .email(EMAIL)
                             .fullName(FULL_NAME)
                             .uid(UID)
                             .build();
  }

  @Test
  public void testAccessors(){
    assertThat(reservation.getEndDate(),is(END_DATE));
    assertThat(reservation.getStartDate(),is(START_DATE));
    assertThat(reservation.getEmail(),is(EMAIL));
    assertThat(reservation.getBookingDate(),is(BOOKING_DATE));
    assertThat(reservation.getUid(),is(UID));
    assertThat(reservation.getFullName(),is(FULL_NAME));
  }
}

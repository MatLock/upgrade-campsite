package com.upgrade.camp.service;

import com.google.common.cache.LoadingCache;
import com.upgrade.camp.controller.request.ReservationRequest;
import com.upgrade.camp.guava.CacheKey;
import com.upgrade.camp.model.Reservation;
import com.upgrade.camp.repository.ReservationRepository;
import com.upgrade.camp.service.exception.AlreadyBookedException;
import com.upgrade.camp.service.exception.ModelConstraintReservation;
import com.upgrade.camp.service.exception.ReservationNotFoundException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ReservationServiceTest {

  private static final String RESERVATION_NOT_FOUND_EX = "Reservation not found";
  private static final String RESERVATION_BOOKING_DATES_HAS_MORE_THAN_3_DAYS = "Camp reservation days cannot be greater than 3 days";
  private static final String RESERVATION_ERROR_SAME_DATE_EX = "Camp cannot be booked for the same day";
  private static final String RESERVATION_THIRTY_DAYS_ADVANCE_EX = "Camp cannot be booked more than 30 days in advance";
  private static final String END_DAY_IS_BEFORE_EX = "End Date should be bigger than Start Date";
  private static final String ALREADY_BOOKED_EX = "Cannot book, due conflicts with other reservations";
  private static final String EMAIL = "someEmail@gmail.com";
  private static final String UID = UUID.randomUUID().toString();
  private static final String NEW_FULL_NAME = "NEW NAME";
  private static final String NEW_EMAIL = "NEW_EMAIL";
  private static final LocalDateTime START_DATE = LocalDateTime.now().plusDays(1);
  private static final LocalDateTime END_DATE = START_DATE.plusDays(3);

  @Mock
  private Reservation reservation;
  @Mock
  private Reservation newReservation;
  @Mock
  private ReservationRequest reservationRequest;
  @Mock
  private ReservationRepository reservationRepository;
  @Mock
  private LoadingCache<CacheKey,List<LocalDateTime>> cache;
  @InjectMocks
  private ReservationService reservationService;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private ConcurrentMap<CacheKey,List<LocalDateTime>> map = new ConcurrentHashMap<>();

  @Before
  public void setUp(){
    when(reservation.getEmail()).thenReturn(EMAIL);

    when(reservation.getStartDate()).thenReturn(START_DATE);
    when(reservation.getEndDate()).thenReturn(END_DATE);

    when(newReservation.getEmail()).thenReturn(NEW_EMAIL);
    when(newReservation.getFullName()).thenReturn(NEW_FULL_NAME);

    doNothing().when(reservation).setUid(anyString());
    doNothing().when(reservation).setBookingDate(any());

    when(reservationRepository.findById(eq(UID))).thenReturn(Optional.of(reservation));

    when(cache.asMap()).thenReturn(map);
    when(cache.getUnchecked(any(CacheKey.class))).thenReturn(new ArrayList<>());

    CacheKey key = CacheKey.builder()
                           .startDate(LocalDateTime.now().plusDays(2))
                           .endDate(LocalDateTime.now().plusDays(5))
                           .build();
    map.put(key,new ArrayList<>());
  }

  @Test
  public void testFindById(){
    Reservation r = reservationService.findById(UID);
    verify(reservationRepository).findById(UID);
    assertThat(r,is(reservation));
  }

  @Test
  public void testFindByIdNotFound(){
    when(reservationRepository.findById(eq(UID))).thenReturn(Optional.empty());
    expectedException.expect(ReservationNotFoundException.class);
    expectedException.expectMessage(RESERVATION_NOT_FOUND_EX);
    reservationService.findById(UID);
    verify(reservationRepository).findById(eq(UID));
  }

  @Test
  public void testCreateReservation(){
    reservationService.createReservation(reservation);
    verify(reservationRepository).save(eq(reservation));
    verify(cache).invalidateAll(any());
  }

  @Test
  public void testCreationFailsDueModelConstraint(){
    when(reservation.getEndDate()).thenReturn(LocalDateTime.now().plusDays(5));
    expectedException.expect(ModelConstraintReservation.class);
    expectedException.expectMessage(RESERVATION_BOOKING_DATES_HAS_MORE_THAN_3_DAYS);
    reservationService.createReservation(reservation);
    verify(reservationRepository,never()).save(any());
  }

  @Test
  public void testCreationFailsDueModelConstraint2(){
    when(reservation.getStartDate()).thenReturn(LocalDateTime.now());
    expectedException.expect(ModelConstraintReservation.class);
    expectedException.expectMessage(RESERVATION_ERROR_SAME_DATE_EX);
    reservationService.createReservation(reservation);
    verify(reservationRepository,never()).save(any());
  }

  @Test
  public void testCreationFailsDueModelConstraint3(){
    when(reservation.getStartDate()).thenReturn(LocalDateTime.now().plusMonths(2));
    expectedException.expect(ModelConstraintReservation.class);
    expectedException.expectMessage(RESERVATION_THIRTY_DAYS_ADVANCE_EX);
    reservationService.createReservation(reservation);
    verify(reservationRepository,never()).save(any());
  }

  @Test
  public void testCreationFailsDueModelConstraint4(){
    when(reservation.getEndDate()).thenReturn(LocalDateTime.now().plusMonths(-2));
    expectedException.expect(ModelConstraintReservation.class);
    expectedException.expectMessage(END_DAY_IS_BEFORE_EX);
    reservationService.createReservation(reservation);
    verify(reservationRepository,never()).save(any());
  }

  @Test
  public void testCreateReservationOverlaps(){
    when(reservationRepository.countReservationThatOverlapsWith(eq(START_DATE),eq(END_DATE))).thenReturn(1l);
    expectedException.expect(AlreadyBookedException.class);
    expectedException.expectMessage(ALREADY_BOOKED_EX);
    reservationService.createReservation(reservation);
    verify(reservationRepository,never()).save(any());
    verify(reservationRepository).countReservationThatOverlapsWith(any(),any());
  }

  @Test
  public void testDelete(){
    reservationService.deleteReservation(UID,EMAIL);
    reservationRepository.delete(any());
    reservationRepository.findById(eq(UID));
  }

  @Test
  public void testDeleteNotFound(){
    when(reservationRepository.findById(UID)).thenReturn(Optional.empty());
    expectedException.expect(ReservationNotFoundException.class);
    expectedException.expectMessage(RESERVATION_NOT_FOUND_EX);
    reservationService.deleteReservation(UID,EMAIL);
    verify(reservationRepository).findById(eq(UID));
    verify(reservationRepository,never()).delete(any());
  }

  @Test
  public void testDeleteWithNoOwnership(){
    when(reservation.getEmail()).thenReturn("");
    expectedException.expect(ReservationNotFoundException.class);
    expectedException.expectMessage(RESERVATION_NOT_FOUND_EX);
    reservationService.deleteReservation(UID,EMAIL);
    verify(reservationRepository).findById(UID);
    verify(reservationRepository,never()).delete(any());
  }

  @Test
  public void testUpdateEntity(){
    when(reservationRepository.save(any())).thenReturn(newReservation);
    Reservation reservation = reservationService.updateReservation(reservationRequest,UID);
    verify(reservationRepository).save(any());
    assertThat(reservation.getEmail(),is(NEW_EMAIL));
    assertThat(reservation.getFullName(),is(NEW_FULL_NAME));
  }

  @Test
  public void testCannotUpdate(){
    when(reservationRepository.countReservationThatOverlapsWith(eq(START_DATE),eq(END_DATE))).thenReturn(1l);
    expectedException.expect(AlreadyBookedException.class);
    expectedException.expectMessage(ALREADY_BOOKED_EX);
    reservationService.updateReservation(reservationRequest,UID);
    verify(reservationRepository,never()).save(any());
  }

  @Test
  public void testCheckAvailability(){
    List<LocalDateTime> list = reservationService.findAvailability(START_DATE,END_DATE);
    verify(cache).getUnchecked(any());
    assertThat(list,is(notNullValue()));
  }

}

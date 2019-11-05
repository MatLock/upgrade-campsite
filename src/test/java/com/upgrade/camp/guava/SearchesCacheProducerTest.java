package com.upgrade.camp.guava;

import com.google.common.cache.LoadingCache;
import com.upgrade.camp.model.Reservation;
import com.upgrade.camp.repository.ReservationRepository;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static org.hamcrest.CoreMatchers.is;

@RunWith(MockitoJUnitRunner.class)
public class SearchesCacheProducerTest {

  @Mock
  private Reservation reservation;
  @Mock
  private ReservationRepository reservationRepository;
  @InjectMocks
  private SearchesCacheProducer cacheProducer;
  private LoadingCache<CacheKey,List<LocalDateTime>> cache;

  private LocalDateTime start;
  private LocalDateTime end;
  private CacheKey cacheKey;
  private List<LocalDateTime> freeDates;

  @Before
  public void setUp(){
    cache = cacheProducer.buildCache();
    start = LocalDateTime.of(2019,11,1,12,0,0,0);
    end = LocalDateTime.of(2019,11,30,12,0,0,0);
    LocalDateTime d1 = LocalDateTime.of(2019,11,1,12,0,0,0);
    LocalDateTime d2 = LocalDateTime.of(2019,11,5,12,0,0,0);
    freeDates = new ArrayList<>();

    cacheKey = CacheKey.builder()
                       .startDate(start)
                       .endDate(end)
                       .build();

    when(reservation.getStartDate()).thenReturn(d1);
    when(reservation.getEndDate()).thenReturn(d2);
    when(reservationRepository.getAllReservationBetween(start,end)).thenReturn(Arrays.asList(reservation));
  }

  @Test
  public void testObtainAvailability(){
    List<LocalDateTime> freeDates =  cache.getUnchecked(cacheKey);
    verify(reservationRepository).getAllReservationBetween(eq(start),eq(end));
    assertThat(freeDates.size(),is(25));
  }

  @Test
  public void testObtainAvailabilityUsingCallingRepositoryOnlyOnce(){
    List<LocalDateTime> freeDates =  cache.getUnchecked(cacheKey);
    cache.getUnchecked(cacheKey);
    verify(reservationRepository).getAllReservationBetween(eq(start),eq(end));
    assertThat(freeDates.size(),is(25));
  }

  @Test
  public void testObtainAvailabilityWhenThereIsNoReservation(){
    when(reservationRepository.getAllReservationBetween(any(),any())).thenReturn(new ArrayList<>());
    List<LocalDateTime>freeDates = cache.getUnchecked(cacheKey);
    verify(reservationRepository).getAllReservationBetween(any(),any());
    assertThat(freeDates.size(),is(29));
  }



}

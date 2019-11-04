package com.upgrade.camp.guava;

import com.upgrade.camp.model.Reservation;
import com.upgrade.camp.repository.ReservationRepository;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.time.temporal.ChronoUnit.DAYS;

@Configuration
@ComponentScan(value = "com.upgrade.camp")
public class SearchesCacheProducer {

  @Autowired
  private ReservationRepository reservationRepository;

  @Bean
  public LoadingCache<CacheKey,List<LocalDateTime>> buildCache(){
    return CacheBuilder.newBuilder()
            .maximumSize(1000l)
            .expireAfterAccess(120, TimeUnit.SECONDS)
            .build(CacheLoader.from(key -> findAvailability(key.getStartDate(),key.getEndDate())));
  }

  private List<LocalDateTime> findAvailability(LocalDateTime startDate, LocalDateTime endDate){
    List<Reservation> reservations = reservationRepository.getAllReservationBetween(startDate,endDate);
    if(reservations.isEmpty()){
      return getAllDatesBetween(startDate,endDate);
    }
    return obtainAvailabilityBetween(startDate,endDate,reservations);
  }

  private List<LocalDateTime> getAllDatesBetween(LocalDateTime startDate,LocalDateTime endDate){
    List<LocalDateTime> dates = new ArrayList<>();
    IntStream.range(0,(int)DAYS.between(startDate.withSecond(0),endDate.withSecond(0)))
            .forEach( (i) -> {
              LocalDateTime date = startDate.plusDays(i).withHour(12).withMinute(0).withSecond(0).withNano(0);
              dates.add(date);
            });
    return dates;
  }

  private List<LocalDateTime> obtainAvailabilityBetween(LocalDateTime startDate, LocalDateTime endDate,List<Reservation> reservations){
    List<LocalDateTime> bookedDates = reservations.stream()
            .map(reservation -> getAllDatesBetween(reservation.getStartDate(),reservation.getEndDate()))
            .flatMap(List::stream)
            .collect(Collectors.toList());
    List<LocalDateTime> dates = getAllDatesBetween(startDate,endDate);
    return dates.stream()
            .filter(e -> !bookedDates.contains(e))
            .collect(Collectors.toList());
  }

}

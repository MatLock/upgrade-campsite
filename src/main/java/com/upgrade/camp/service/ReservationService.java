package com.upgrade.camp.service;

import com.upgrade.camp.guava.CacheKey;
import com.upgrade.camp.model.Reservation;
import com.upgrade.camp.repository.ReservationRepository;
import com.upgrade.camp.controller.request.ReservationRequest;
import com.upgrade.camp.service.exception.AlreadyBookedException;
import com.upgrade.camp.service.exception.ModelConstraintReservation;
import com.upgrade.camp.service.exception.ReservationNotFoundException;
import com.google.common.cache.LoadingCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static  java.time.temporal.ChronoUnit.DAYS;

@Service
public class ReservationService {

  private static final String EMAIL_REGEX = "^(.+)@(.+)$";

  @Autowired
  private ReservationRepository reservationRepository;
  @Autowired
  private LoadingCache<CacheKey,List<LocalDateTime>> loadingCache;

  public Reservation findById(String id){
    return findReservation(id);
  }

  public List<LocalDateTime> findAvailability(LocalDateTime startDate, LocalDateTime endDate){
    return loadingCache.getUnchecked(CacheKey.builder()
                                             .startDate(startDate)
                                             .endDate(endDate)
                                             .build());
  }

  @Transactional
  public void createReservation(Reservation reservation){
    setUUIDAndBookingDate(reservation);
    validateConstraintsToBook(reservation);
    checkIfReservationOverlaps(reservation);
    reservationRepository.save(reservation);
    checkForDirtyValues(reservation);
  }

  @Transactional
  public void deleteReservation(String id, String email){
    Optional<Reservation> reservation = reservationRepository.findById(id);
    if(reservation.isPresent()){
      checkOwnership(reservation.get(),email);
      reservationRepository.delete(reservation.get());
      return;
    }
    throw new ReservationNotFoundException("Reservation not found");
  }

  @Transactional
  public Reservation updateReservation(ReservationRequest reservationRequest, String id){
    Reservation reservation = findById(id);
    try{
      reservation.replaceWith(reservationRequest);
      validateConstraintsToBook(reservation);
      checkIfReservationOverlaps(reservation);
      return reservationRepository.save(reservation);
    }finally {
      checkForDirtyValues(reservation);
    }
  }

  private Reservation findReservation(String id){
    Optional<Reservation>reservation = reservationRepository.findById(id);
    if(!reservation.isPresent()){
      throw new ReservationNotFoundException("Reservation not found");
    }
    return reservation.get();
  }

  private void setUUIDAndBookingDate(Reservation reservation){
    String uuid = UUID.randomUUID().toString();
    reservation.setUid(uuid);
    reservation.setBookingDate(LocalDateTime.now());
  }

  private void checkOwnership(Reservation reservation,String email){
    if(!reservation.getEmail().equals(email)){
      throw new ReservationNotFoundException("Reservation not found");
    }
  }

  private void validateConstraintsToBook(Reservation reservation){
    if(!reservation.getEmail().matches(EMAIL_REGEX)){
      throw new ModelConstraintReservation("Not a valid Email");
    }
    if((DAYS.between(reservation.getStartDate(),reservation.getEndDate())) > 3){
      throw new ModelConstraintReservation("Camp reservation days cannot be greater than 3 days");
    }
    if(DAYS.between(LocalDateTime.now(),reservation.getStartDate()) > 30){
      throw new ModelConstraintReservation("Camp cannot be booked more than 30 days in advance");
    }
    LocalDateTime today = LocalDateTime.now().withHour(12).withMinute(0).withSecond(01).withNano(0);
    if(DAYS.between(today,reservation.getStartDate()) < 1){
      throw new ModelConstraintReservation("Camp cannot be booked for the same day");
    }
    if(reservation.getEndDate().isBefore(reservation.getStartDate())){
      throw new ModelConstraintReservation("End Date should be bigger than Start Date");
    }
  }

  private void checkIfReservationOverlaps(Reservation reservation){
    if(reservationRepository.countReservationThatOverlapsWith(reservation.getStartDate(),reservation.getEndDate()) >= 1){
      throw new AlreadyBookedException("Cannot book, due conflicts with other reservations");
    }
  }

  private void checkForDirtyValues(Reservation reservation){
   List<CacheKey> keys = loadingCache.asMap().keySet().stream().filter( key ->
     dateBetween(key.getStartDate(),reservation.getStartDate(),reservation.getEndDate()) ||
     dateBetween(key.getEndDate(),reservation.getStartDate(),reservation.getEndDate())
      ).collect(Collectors.toList());
   loadingCache.invalidateAll(keys);
  }

  private Boolean dateBetween(LocalDateTime target, LocalDateTime start, LocalDateTime end){
    return target.isAfter(start) && target.isBefore(end);
  }

}

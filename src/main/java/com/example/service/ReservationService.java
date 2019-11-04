package com.example.service;

import com.example.model.Reservation;
import com.example.repository.ReservationRepository;
import com.example.controller.request.ReservationRequest;
import com.example.service.exception.AlreadyBookedException;
import com.example.service.exception.ModelConstraintReservation;
import com.example.service.exception.ReservationNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static  java.time.temporal.ChronoUnit.DAYS;

@Service
public class ReservationService {

  @Autowired
  private ReservationRepository reservationRepository;

  public Reservation findById(String id){
    return findReservation(id);
  }

  public List<LocalDateTime> findAvaliability(LocalDateTime startDate, LocalDateTime endDate){
    List<Reservation> reservations = reservationRepository.getAllReservationBetween(startDate,endDate);
    if(reservations.isEmpty()){
      return getAllDatesBetween(startDate,endDate);
    }
    return obtainAvailabilityBetween(startDate,endDate,reservations);
  }

  @Transactional
  public void createReservation(Reservation reservation){
    setUUIDAndBookingDate(reservation);
    validateConstraintsToBook(reservation);
    checkIfReservationOverlaps(reservation);
    reservationRepository.save(reservation);
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
    reservation.replaceWith(reservationRequest);
    validateConstraintsToBook(reservation);
    checkIfReservationOverlaps(reservation);
    return reservationRepository.save(reservation);
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
      throw new ReservationNotFoundException("Reservation not found for the given email");
    }
  }

  private void validateConstraintsToBook(Reservation reservation){
    if((DAYS.between(reservation.getEndDate(),reservation.getStartDate())) > 3){
      throw new ModelConstraintReservation("Camp reservation days cannot be greater than 3 days");
    }
    if(DAYS.between(reservation.getStartDate(),LocalDateTime.now()) > 30){
      throw new ModelConstraintReservation("Camp cannot be booked more than 30 days in advance");
    }
    if(DAYS.between(reservation.getStartDate(),LocalDateTime.now()) < 1){
      throw new ModelConstraintReservation("Camp cannot be booked for the same day");
    }
  }

  private void checkIfReservationOverlaps(Reservation reservation){
    if(reservationRepository.reservationOverlapsWith(reservation.getStartDate(),reservation.getEndDate())){
      throw new AlreadyBookedException("Cannot book, due conflicts with other reservations");
    }
  }

  private List<LocalDateTime> getAllDatesBetween(LocalDateTime startDate,LocalDateTime endDate){
    List<LocalDateTime> dates = new ArrayList<>();
    IntStream.range(0,(int)DAYS.between(startDate,endDate))
             .forEach( (i) -> dates.add(startDate.plusDays(1)));
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

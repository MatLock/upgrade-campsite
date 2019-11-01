package com.example.service;

import com.example.model.Reservation;
import com.example.repository.ReservationRepository;
import com.example.service.exception.ModelConstraintReservation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static  java.time.temporal.ChronoUnit.DAYS;

@Service
public class ReservationService {

  @Autowired
  private ReservationRepository reservationRepository;

  public Reservation findById(String id){
    return reservationRepository.findById(id).get();
  }

  @Transactional
  public void createReservation(Reservation reservation){
    setUUID(reservation);
    validateConstraintsToBook(reservation);
   // List<Reservation> reservations = reservationRepository.findByStartDateBetween();
    reservationRepository.save(reservation);
  }

  private void setUUID(Reservation reservation){
    String uuid = UUID.randomUUID().toString();
    reservation.setUid(uuid);
  }

  private void validateConstraintsToBook(Reservation reservation){
    if((DAYS.between(reservation.getEndDate(),reservation.getStartDate())) > 3){
      throw new ModelConstraintReservation("camp reservation days cannot be greater than 3 days");
    }

  }
}

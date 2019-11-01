package com.example.service;

import com.example.model.Reservation;
import com.example.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ReservationService {

  @Autowired
  private ReservationRepository reservationRepository;

  public Reservation findById(String id){
    return reservationRepository.findById(id).get();
  }

  @Transactional
  public void createReservation(Reservation reservation){
    String uuid = UUID.randomUUID().toString();
    reservation.setUid(uuid);
    reservationRepository.save(reservation);
  }
}

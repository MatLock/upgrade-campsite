package com.example.model;

import com.example.controller.request.ReservationRequest;
import lombok.Builder;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity(name = "RESERVATION")
@Data
@Builder
public class Reservation {

  @Column(name = "ID",unique = true)
  private String uid;
  @Id
  @Column(name = "START_DATE")
  private LocalDateTime startDate;
  @Column(name = "END_DATE")
  private LocalDateTime endDate;
  @Column(name = "BOOKING_DATE")
  private LocalDateTime bookingDate;
  @Column(name = "EMAIL")
  private String email;
  @Column(name = "FULL_NAME")
  private String fullName;

  public void replaceWith(ReservationRequest reservationRequest){
    this.email = reservationRequest.getEmail();
    this.startDate = reservationRequest.getStartDate();
    this.endDate = reservationRequest.getEndDate();
    this.fullName = reservationRequest.getFullName();
  }

}

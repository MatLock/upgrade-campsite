package com.example.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity(name = "RESERVATION")
@Data
public class Reservation {

  @Id
  @Column(name = "ID")
  private String uid;
  @Column(name = "START_DATE")
  private LocalDateTime startDate;
  @Column(name = "END_DATE")
  private LocalDateTime endDate;
  @Column(name = "BOOKING_DATE")
  private LocalDateTime bookingDate;
  @Column(name = "CANCELATION_DATE")
  private LocalDateTime cancelationDate;
  @Column(name = "EMAIL")
  private String email;

}

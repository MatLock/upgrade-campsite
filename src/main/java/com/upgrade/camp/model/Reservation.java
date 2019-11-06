package com.upgrade.camp.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.upgrade.camp.controller.request.ReservationRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity(name = "RESERVATION")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

  @Id
  @Column(name = "UID",unique = true)
  private String uid;
  @Column(name = "START_DATE",unique = true)
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime startDate;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @Column(name = "END_DATE",unique = true)
  private LocalDateTime endDate;
  @Column(name = "BOOKING_DATE")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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

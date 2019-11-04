package com.example.controller.response;

import com.example.model.Reservation;
import lombok.Builder;
import lombok.Data;

@Data
public class ReservationResponse extends BasicResponse {

  private Reservation response;

  @Builder
  public ReservationResponse(Reservation reservation, String msg, Boolean error){
      super(msg,error);
      response = reservation;
  }

}

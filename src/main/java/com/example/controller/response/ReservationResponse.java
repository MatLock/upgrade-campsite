package com.example.controller.response;

import com.example.model.Reservation;
import lombok.Builder;

public class ReservationResponse extends BasicResponse {

  @Builder
  public ReservationResponse(Reservation reservation, String msg, Boolean error){
      super(msg,reservation,error);
  }

}

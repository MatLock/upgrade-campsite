package com.example.controller.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class AvailableDaysResponse extends  BasicResponse{

  @Builder
  public AvailableDaysResponse(List<LocalDateTime> days, String msg, Boolean error){
    super(msg,days,error);
  }
}

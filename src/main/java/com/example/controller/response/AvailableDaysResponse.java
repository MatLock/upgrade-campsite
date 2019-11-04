package com.example.controller.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AvailableDaysResponse extends  BasicResponse{

  private List<LocalDateTime> response;

  @Builder
  public AvailableDaysResponse(List<LocalDateTime> days, String msg, Boolean error){
    super(msg,error);
    response = days;
  }
}

package com.example.controller.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AvailableDaysResponse extends  BasicResponse{

  @JsonDeserialize(using = LocalDateDeserializer.class)
  private List<LocalDateTime> response;

  @Builder
  public AvailableDaysResponse(List<LocalDateTime> days, String msg, Boolean error){
    super(msg,error);
    response = days;
  }
}

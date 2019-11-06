package com.upgrade.camp.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
public class AvailableDaysResponse extends  BasicResponse{

  @JsonDeserialize(using = LocalDateDeserializer.class)
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @Getter
  @Setter
  private List<LocalDateTime> response;

  public AvailableDaysResponse(List<LocalDateTime> days, String msg, Boolean error){
    super(msg,error);
    response = days;
  }
}

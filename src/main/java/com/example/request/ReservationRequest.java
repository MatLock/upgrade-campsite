package com.example.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ReservationRequest {

  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private String email;
}

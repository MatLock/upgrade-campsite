package com.upgrade.camp.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
public class ReservationRequest {

  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private String email;
  private String fullName;
}

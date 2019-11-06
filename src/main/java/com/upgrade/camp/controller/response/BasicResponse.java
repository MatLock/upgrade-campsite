package com.upgrade.camp.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
public class BasicResponse {

  @Getter
  @Setter
  private String message;
  @Getter
  @Setter
  private Boolean error;
}

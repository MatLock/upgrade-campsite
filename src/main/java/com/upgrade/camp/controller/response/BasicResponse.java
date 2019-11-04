package com.upgrade.camp.controller.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BasicResponse {

  private String message;
  private Boolean error;
}
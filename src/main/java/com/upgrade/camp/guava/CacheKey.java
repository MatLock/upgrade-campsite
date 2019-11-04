package com.upgrade.camp.guava;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CacheKey {

  private LocalDateTime startDate;
  private LocalDateTime endDate;
}

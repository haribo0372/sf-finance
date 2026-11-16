package ru.osipov.sf.finance.models.entity;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Budget {

  private UUID id;
  private UUID userId;
  private Integer sum;
  private String category;

  @Override
  public String toString() {
    return "Budget{" + "category='" + category + '\'' + ", sum=" + sum + '}';
  }
}

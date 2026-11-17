package ru.osipov.sf.finance.models.entity;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Модель данных бюджета */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Budget {

  private UUID id;
  private UUID userId;
  private Integer sum;
  private String category;

  public Budget(UUID id, UUID userId, Integer sum, Category category) {
    this.id = id;
    this.userId = userId;
    this.sum = sum;
    this.category = category.getName();
  }

  @Override
  public String toString() {
    return "Budget{" + "category='" + category + '\'' + ", sum=" + sum + '}';
  }
}

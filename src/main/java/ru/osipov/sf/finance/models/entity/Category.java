package ru.osipov.sf.finance.models.entity;

import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {

  private UUID id;
  private String name;
  private UUID createdBy;
  private LocalDate createdAt;

  @Override
  public String toString() {
    return "Category{" + "name='" + name + '\'' + '}';
  }
}

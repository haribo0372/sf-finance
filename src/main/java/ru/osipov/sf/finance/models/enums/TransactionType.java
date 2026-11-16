package ru.osipov.sf.finance.models.enums;

import java.util.Objects;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransactionType implements Labeled {
  INCOME(1, "Доход"),
  EXPENSE(2, "Расход");

  private final Integer number;
  private final String label;

  public static Optional<TransactionType> getByNumber(Integer number) {
    for (var type : TransactionType.values()) {
      if (Objects.equals(number, type.number)) return Optional.of(type);
    }
    return Optional.empty();
  }

  public static boolean existByNumber(Integer number) {
    for (var type : TransactionType.values()) {
      if (Objects.equals(number, type.number)) return true;
    }
    return false;
  }
}

package ru.osipov.sf.finance.models.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Тип дранзакции: Доход/Расход */
@Getter
@AllArgsConstructor
public enum TransactionType implements Labeled {
  INCOME(1, "Доход"),
  EXPENSE(2, "Расход");

  private final Integer number;
  private final String label;
}

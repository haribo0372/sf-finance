package ru.osipov.sf.finance.models.entity;

import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.osipov.sf.finance.models.enums.TransactionType;

/** Модель данных транзакции */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

  private UUID id;
  private UUID userId;
  private Integer sum;
  private TransactionType type;
  private String category;
  private LocalDate date;

  @Override
  public String toString() {
    return "Transaction{"
        + "category='"
        + category
        + '\''
        + ", type='"
        + type.getLabel()
        + '\''
        + ", sum="
        + sum
        + '}';
  }
}

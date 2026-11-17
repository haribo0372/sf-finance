package ru.osipov.sf.finance.models.result;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.osipov.sf.finance.models.entity.Transaction;
import ru.osipov.sf.finance.models.warning.Warning;

/** Результат регистрации транзакции: сама транзакция и список уведомлений о превышениях. */
@Getter
@RequiredArgsConstructor
public class TransactionResult {

  private final Transaction transaction;
  private final List<Warning> warnings;
}

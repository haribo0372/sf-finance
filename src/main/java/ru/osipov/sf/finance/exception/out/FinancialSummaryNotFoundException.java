package ru.osipov.sf.finance.exception.out;

import ru.osipov.sf.finance.exception.base.BaseException;

public class FinancialSummaryNotFoundException extends BaseException {

  public FinancialSummaryNotFoundException() {
    super("Данные не найдены");
  }
}

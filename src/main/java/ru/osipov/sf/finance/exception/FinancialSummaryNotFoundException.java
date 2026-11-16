package ru.osipov.sf.finance.exception;

public class FinancialSummaryNotFoundException extends BaseException {

  public FinancialSummaryNotFoundException() {
    super("Данные не найдены");
  }
}

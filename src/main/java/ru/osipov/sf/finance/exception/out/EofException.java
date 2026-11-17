package ru.osipov.sf.finance.exception.out;

import ru.osipov.sf.finance.exception.base.BaseException;

public class EofException extends BaseException {

  public EofException() {
    super("Ввод завершён");
  }
}

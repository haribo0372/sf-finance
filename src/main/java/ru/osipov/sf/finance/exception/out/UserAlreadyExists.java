package ru.osipov.sf.finance.exception.out;

import ru.osipov.sf.finance.exception.base.BaseException;

public class UserAlreadyExists extends BaseException {

  public UserAlreadyExists(String message) {
    super(message);
  }
}

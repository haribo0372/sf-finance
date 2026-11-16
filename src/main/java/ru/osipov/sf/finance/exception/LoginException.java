package ru.osipov.sf.finance.exception;

import ru.osipov.sf.finance.models.entity.Category;

public class LoginException extends BaseException {

  public LoginException(String message) {
    super(message);
  }
}

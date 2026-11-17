package ru.osipov.sf.finance.exception.base;

/** Базовый класс для кастомных исключений приложения. */
public class BaseException extends RuntimeException {

  /**
   * Создаёт исключение с указанным сообщением.
   *
   * @param message сообщение об ошибке
   */
  public BaseException(String message) {
    super(message);
  }
}

package ru.osipov.sf.finance.exception;

public class CategoryNotFoundException extends BaseException {

  public CategoryNotFoundException(String name) {
    super(String.format("Категория '%s' не найдена", name));
  }
}

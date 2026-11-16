package ru.osipov.sf.finance.exception;

public class CategoryNotFoundException extends BaseException {

  public CategoryNotFoundException(String name) {
    super(String.format("Категория не найдена name='%s'", name));
  }
}

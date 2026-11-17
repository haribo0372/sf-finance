package ru.osipov.sf.finance.exception.out;

import ru.osipov.sf.finance.exception.base.BaseException;

public class CategoryNotFoundException extends BaseException {

  public CategoryNotFoundException(String name) {
    super(String.format("Категория '%s' не найдена", name));
  }
}

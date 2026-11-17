package ru.osipov.sf.finance.exception.out;

import ru.osipov.sf.finance.exception.base.BaseException;

public class CategoryAlreadyExistsException extends BaseException {

  public CategoryAlreadyExistsException(String name) {
    super(String.format("Категория '%s' уже существует", name));
  }
}

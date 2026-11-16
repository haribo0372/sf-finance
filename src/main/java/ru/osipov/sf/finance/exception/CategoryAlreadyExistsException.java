package ru.osipov.sf.finance.exception;

public class CategoryAlreadyExistsException extends BaseException {

  public CategoryAlreadyExistsException(String name) {
    super(String.format("Категория '%s' уже существует", name));
  }
}

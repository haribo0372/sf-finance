package ru.osipov.sf.finance.exception.out;

import ru.osipov.sf.finance.exception.base.BaseException;
import ru.osipov.sf.finance.models.entity.Category;

public class BudgetAlreadyExistsException extends BaseException {

  public BudgetAlreadyExistsException(Category category) {
    super(String.format("Бюджет для категории '%s' уже существует", category.getName()));
  }
}

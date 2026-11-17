package ru.osipov.sf.finance.exception.out;

import ru.osipov.sf.finance.exception.base.BaseException;
import ru.osipov.sf.finance.models.entity.Category;

public class BudgetNotFoundException extends BaseException {

  public BudgetNotFoundException(Category category) {
    super(String.format("Бюджет для категории '%s' не найден", category.getName()));
  }
}

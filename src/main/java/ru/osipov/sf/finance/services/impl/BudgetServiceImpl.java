package ru.osipov.sf.finance.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import ru.osipov.sf.finance.config.ApplicationConfig;
import ru.osipov.sf.finance.exception.CategoryNotFoundException;
import ru.osipov.sf.finance.models.entity.Budget;
import ru.osipov.sf.finance.models.entity.Category;
import ru.osipov.sf.finance.models.entity.Transaction;
import ru.osipov.sf.finance.services.AbstractEntityService;
import ru.osipov.sf.finance.services.BudgetService;
import ru.osipov.sf.finance.services.CategoryService;

public class BudgetServiceImpl extends AbstractEntityService<List<Budget>>
    implements BudgetService {

  private final CategoryService categoryService;

  public BudgetServiceImpl(
      ApplicationConfig applicationConfig, ObjectMapper mapper, CategoryService categoryService) {
    super(
        applicationConfig.getBudgetFile(),
        mapper,
        new TypeReference<Map<String, List<Budget>>>() {});
    this.categoryService = categoryService;
  }

  @Override
  public Budget save(UUID userId, Integer sum, String categoryName) {
    Category category =
        categoryService
            .findByName(categoryName)
            .orElseThrow(() -> new CategoryNotFoundException(categoryName));
    Optional<Budget> budgetOpt = findByUserIdAndCategory(userId, category.getName());
    Budget budget;
    String message;

    if (budgetOpt.isEmpty()) {
      budget = Budget.builder().id(UUID.randomUUID()).userId(userId).build();
      storage.computeIfAbsent(userId.toString(), k -> new ArrayList<>()).add(budget);
      message = "Бюджет успешно установлен: ";
    } else {
      budget = budgetOpt.get();
      message = "Бюджет успешно обновлен: ";
    }

    budget.setCategory(category.getName());
    budget.setSum(sum);
    saveToFile();
    System.out.println(message + budget);
    return budget;
  }

  @Override
  public Optional<Budget> findByUserIdAndCategory(UUID userId, String categoryName) {
    List<Budget> userBudgets = findByUserId(userId);
    return userBudgets.stream()
        .filter(budget -> budget.getCategory().equalsIgnoreCase(categoryName))
        .findFirst();
  }

  @Override
  public List<Budget> findByUserId(UUID userId) {
    List<Budget> budgets = storage.get(userId.toString());
    return budgets != null ? budgets : List.of();
  }
}

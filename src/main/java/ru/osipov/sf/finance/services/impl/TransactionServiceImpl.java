package ru.osipov.sf.finance.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import ru.osipov.sf.finance.config.ApplicationConfig;
import ru.osipov.sf.finance.exception.CategoryNotFoundException;
import ru.osipov.sf.finance.models.entity.Budget;
import ru.osipov.sf.finance.models.entity.Category;
import ru.osipov.sf.finance.models.entity.Transaction;
import ru.osipov.sf.finance.models.enums.TransactionType;
import ru.osipov.sf.finance.services.AbstractEntityService;
import ru.osipov.sf.finance.services.BudgetService;
import ru.osipov.sf.finance.services.CategoryService;
import ru.osipov.sf.finance.services.TransactionService;

public class TransactionServiceImpl extends AbstractEntityService<List<Transaction>>
    implements TransactionService {

  private final BudgetService budgetService;
  private final CategoryService categoryService;

  public TransactionServiceImpl(
      ApplicationConfig applicationConfig,
      ObjectMapper mapper,
      BudgetService budgetService,
      CategoryService categoryService) {
    super(
        applicationConfig.getTransactionFile(),
        mapper,
        new TypeReference<Map<String, List<Transaction>>>() {});
    this.budgetService = budgetService;
    this.categoryService = categoryService;
  }

  @Override
  public Transaction save(UUID userId, Integer sum, TransactionType type, String categoryName) {
    Category category =
        categoryService
            .findByName(categoryName)
            .orElseThrow(() -> new CategoryNotFoundException(categoryName));

    Transaction transaction =
        Transaction.builder()
            .id(UUID.randomUUID())
            .userId(userId)
            .sum(sum)
            .type(type)
            .category(category.getName())
            .build();
    storage.computeIfAbsent(userId.toString(), k -> new ArrayList<>()).add(transaction);
    saveToFile();
    System.out.println("Транзакция успешно зарегистрирована: " + transaction);

    if (type.equals(TransactionType.EXPENSE)) {
      Optional<Budget> budgetOpt =
          budgetService.findByUserIdAndCategory(userId, category.getName());
      budgetOpt.ifPresent(
          budget -> {
            var transactionsByCategory = findByUserIdAndCategory(userId, category.getName());
            var expenseSum = calculateExpenseSum(transactionsByCategory);

            if (budget.getSum() < expenseSum) {
              System.out.printf(
                  "Внимание! Сумма расходов превышает выделенный бюджет:\n\tКатегория='%s',\n\t Сумма расходов=%s,\n\t Бюджет=%s%n",
                  category.getName(), expenseSum, budget.getSum());
            } else if (Objects.equals(budget.getSum(), expenseSum)) {
              System.out.printf(
                  "Внимание! Сумма расходов равна выделенному бюджету:\n\tКатегория='%s',\n\t Сумма расходов=%s,\n\t Бюджет=%s%n",
                  category.getName(), expenseSum, budget.getSum());
            }
          });
    }
    return transaction;
  }

  public Integer calculateExpenseSum(List<Transaction> transactions) {
    Integer sum = 0;
    for (var transaction : transactions) {
      if (transaction.getType().equals(TransactionType.EXPENSE)) {
        sum += transaction.getSum();
      }
    }
    return sum;
  }

  public List<Transaction> findByUserIdAndCategory(UUID userId, String category) {
    List<Transaction> transactions = findByUserId(userId);
    return transactions.stream()
        .filter(transaction -> transaction.getCategory().equalsIgnoreCase(category))
        .collect(Collectors.toList());
  }

  public List<Transaction> findByUserId(UUID userId) {
    List<Transaction> transactions = storage.get(userId.toString());
    return transactions != null ? transactions : List.of();
  }
}

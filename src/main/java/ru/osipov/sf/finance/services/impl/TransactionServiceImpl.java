package ru.osipov.sf.finance.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import ru.osipov.sf.finance.config.ApplicationConfig;
import ru.osipov.sf.finance.exception.out.BudgetAlreadyExistsException;
import ru.osipov.sf.finance.exception.out.BudgetNotFoundException;
import ru.osipov.sf.finance.exception.out.FinancialSummaryNotFoundException;
import ru.osipov.sf.finance.models.aggregate.FinancialSummary;
import ru.osipov.sf.finance.models.aggregate.TransactionData;
import ru.osipov.sf.finance.models.entity.Budget;
import ru.osipov.sf.finance.models.entity.Category;
import ru.osipov.sf.finance.models.entity.Transaction;
import ru.osipov.sf.finance.models.enums.TransactionType;
import ru.osipov.sf.finance.models.result.TransactionResult;
import ru.osipov.sf.finance.models.warning.Warning;
import ru.osipov.sf.finance.services.CategoryService;
import ru.osipov.sf.finance.services.TransactionService;
import ru.osipov.sf.finance.services.base.AbstractEntityService;

public class TransactionServiceImpl extends AbstractEntityService<FinancialSummary>
    implements TransactionService {

  private final CategoryService categoryService;

  public TransactionServiceImpl(
      ApplicationConfig applicationConfig, ObjectMapper mapper, CategoryService categoryService) {
    super(
        applicationConfig.getTransactionFile(),
        mapper,
        new TypeReference<Map<String, FinancialSummary>>() {});
    this.categoryService = categoryService;
  }

  @Override
  public TransactionResult registerTransaction(
      UUID userId, Integer sum, TransactionType type, String categoryName) {
    Category category = categoryService.findByName(categoryName);

    FinancialSummary financialSummary =
        storage.computeIfAbsent(userId.toString(), k -> new FinancialSummary(userId));

    Transaction transaction =
        new Transaction(UUID.randomUUID(), userId, sum, type, category, LocalDate.now());

    financialSummary.addTransaction(category, transaction);
    saveToFile();

    List<Warning> warnings = new ArrayList<>();
    if (type == TransactionType.EXPENSE) {
      checkExpensesExceedIncome(financialSummary).ifPresent(warnings::add);
      checkExpensesExceedBudget(financialSummary, category).ifPresent(warnings::add);
    }

    return new TransactionResult(transaction, warnings);
  }

  @Override
  public Budget registerBudget(UUID userId, Integer sum, String categoryName) {
    Category category = categoryService.findByName(categoryName);

    FinancialSummary financialSummary =
        storage.computeIfAbsent(userId.toString(), k -> new FinancialSummary(userId));

    TransactionData txData =
        financialSummary
            .getTransactionDataByCategory(category)
            .orElseGet(
                () -> {
                  TransactionData newTxData = new TransactionData(category);
                  financialSummary.addTransactionData(newTxData);
                  return newTxData;
                });

    if (txData.getBudget() != null) throw new BudgetAlreadyExistsException(category);

    Budget budget = new Budget(UUID.randomUUID(), userId, sum, category);
    txData.setBudget(budget);

    saveToFile();
    return budget;
  }

  @Override
  public void removeBudget(UUID userId, String categoryName) {
    Category category = categoryService.findByName(categoryName);
    FinancialSummary financialSummary =
        storage.computeIfAbsent(userId.toString(), k -> new FinancialSummary(userId));

    financialSummary
        .getTransactionDataByCategory(category)
        .filter(td -> td.getBudget() != null)
        .orElseThrow(() -> new BudgetNotFoundException(category))
        .setBudget(null);
    saveToFile();
  }

  @Override
  public TransactionData findTransactionsByUserIdAndCategory(UUID userId, String categoryName) {
    Category category = categoryService.findByName(categoryName);
    FinancialSummary summary = storage.get(userId.toString());
    if (summary == null) throw new FinancialSummaryNotFoundException();

    return summary
        .getTransactionDataByCategory(category)
        .orElseGet(() -> new TransactionData(category));
  }

  @Override
  public FinancialSummary findFinancialSummaryByUserId(UUID userId) {
    FinancialSummary summary = storage.get(userId.toString());
    if (summary == null) throw new FinancialSummaryNotFoundException();
    return summary;
  }

  private Optional<Warning> checkExpensesExceedIncome(FinancialSummary financialSummary) {
    int incomeSum = financialSummary.getIncomeSum();
    int expenseSum = financialSummary.getExpenseSum();
    if (incomeSum >= expenseSum) {
      return Optional.empty();
    }
    return Optional.of(Warning.expensesExceedIncome(incomeSum, expenseSum));
  }

  private Optional<Warning> checkExpensesExceedBudget(
      FinancialSummary financialSummary, Category category) {
    Optional<TransactionData> txDataOpt = financialSummary.getTransactionDataByCategory(category);
    if (txDataOpt.isEmpty()) {
      return Optional.empty();
    }
    TransactionData transactionData = txDataOpt.get();
    Budget budget = transactionData.getBudget();
    if (budget == null) {
      return Optional.empty();
    }

    int budgetSum = budget.getSum();
    int expenseSum = transactionData.getExpenseSum();
    if (budgetSum < expenseSum) {
      return Optional.of(Warning.expensesExceedBudget(category.getName(), expenseSum, budgetSum));
    }
    if (budgetSum == expenseSum) {
      return Optional.of(Warning.budgetExhausted(category.getName(), expenseSum, budgetSum));
    }
    return Optional.empty();
  }
}

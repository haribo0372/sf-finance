package ru.osipov.sf.finance.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import ru.osipov.sf.finance.config.ApplicationConfig;
import ru.osipov.sf.finance.exception.BudgetAlreadyExistsException;
import ru.osipov.sf.finance.exception.BudgetNotFoundException;
import ru.osipov.sf.finance.exception.FinancialSummaryNotFoundException;
import ru.osipov.sf.finance.models.FinancialSummary;
import ru.osipov.sf.finance.models.TransactionData;
import ru.osipov.sf.finance.models.entity.Budget;
import ru.osipov.sf.finance.models.entity.Category;
import ru.osipov.sf.finance.models.entity.Transaction;
import ru.osipov.sf.finance.models.enums.TransactionType;
import ru.osipov.sf.finance.services.AbstractEntityService;
import ru.osipov.sf.finance.services.CategoryService;
import ru.osipov.sf.finance.services.TransactionService;

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
  public Transaction registerTransaction(
      UUID userId, Integer sum, TransactionType type, String categoryName) {
    Category category = categoryService.findByName(categoryName);

    FinancialSummary financialSummary =
        storage.computeIfAbsent(userId.toString(), k -> new FinancialSummary(userId));

    Transaction transaction =
        Transaction.builder()
            .id(UUID.randomUUID())
            .userId(userId)
            .sum(sum)
            .type(type)
            .category(category.getName())
            .date(LocalDate.now())
            .build();

    financialSummary.addTransaction(transaction);
    saveToFile();

    if (type == TransactionType.EXPENSE) {
      notifyIfExpensesExceedIncome(financialSummary);
      notifyIfExpensesExceedBudget(financialSummary, category);
    }

    return transaction;
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

  private void notifyIfExpensesExceedBudget(FinancialSummary financialSummary, Category category) {
    Optional<TransactionData> txDataOpt = financialSummary.getTransactionDataByCategory(category);
    if (txDataOpt.isEmpty()) return;

    TransactionData transactionData = txDataOpt.get();
    Budget budget = transactionData.getBudget();
    if (budget == null) return;

    Integer budgetSum = budget.getSum();
    Integer expenseSum = transactionData.getExpenseSum();
    if (budgetSum < expenseSum) {
      int overrun = expenseSum - budgetSum;
      System.out.println();
      System.out.println("!!! ВНИМАНИЕ: превышение бюджета !!!");
      System.out.println("----------------------------------");
      System.out.printf("Категория:        %s%n", category.getName());
      System.out.printf("Бюджет:           %s%n", budgetSum);
      System.out.printf("Сумма расходов:   %s%n", expenseSum);
      System.out.printf("Превышение:       %s%n", overrun);
      System.out.println("==================================");
    } else if (Objects.equals(budgetSum, expenseSum)) {
      System.out.println();
      System.out.println("!!! ВНИМАНИЕ: бюджет исчерпан !!!");
      System.out.println("----------------------------------");
      System.out.printf("Категория:        %s%n", category.getName());
      System.out.printf("Бюджет:           %s%n", budgetSum);
      System.out.printf("Сумма расходов:   %s%n", expenseSum);
      System.out.printf("Остаток:          %s%n", 0);
      System.out.println("==================================");
    }
  }

  private void notifyIfExpensesExceedIncome(FinancialSummary financialSummary) {
    Integer incomeSum = financialSummary.getIncomeSum();
    Integer expenseSum = financialSummary.getExpenseSum();
    if (incomeSum < expenseSum) {
      int deficit = expenseSum - incomeSum;
      System.out.println();
      System.out.println("!!! ВНИМАНИЕ: расходы превышают доходы !!!");
      System.out.println("-----------------------------------------");
      System.out.printf("Сумма доходов:    %s%n", incomeSum);
      System.out.printf("Сумма расходов:   %s%n", expenseSum);
      System.out.printf("Дефицит:          %s%n", deficit);
      System.out.println("=========================================");
    }
  }
}

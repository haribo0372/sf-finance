package ru.osipov.sf.finance.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.osipov.sf.finance.config.ApplicationConfig;
import ru.osipov.sf.finance.config.JsonConfig;
import ru.osipov.sf.finance.exception.out.BudgetAlreadyExistsException;
import ru.osipov.sf.finance.exception.out.BudgetNotFoundException;
import ru.osipov.sf.finance.exception.out.CategoryNotFoundException;
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

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

  private static final UUID USER_ID = UUID.randomUUID();
  private static final String CATEGORY_NAME = "ЕДА";

  @TempDir Path tempDir;

  @Mock private ApplicationConfig applicationConfig;
  @Mock private CategoryService categoryService;

  private final JsonConfig jsonConfig = new JsonConfig();
  private TransactionServiceImpl service;
  private Category category;

  @BeforeEach
  void setUp() {
    when(applicationConfig.getTransactionFile())
        .thenReturn(tempDir.resolve("transactions.json").toString());

    service =
        new TransactionServiceImpl(applicationConfig, jsonConfig.getMapper(), categoryService);

    category = new Category(UUID.randomUUID(), CATEGORY_NAME, USER_ID, LocalDate.now());
  }

  @Test
  void registerTransaction_income_success() {
    when(categoryService.findByName(CATEGORY_NAME)).thenReturn(category);

    TransactionResult result =
        service.registerTransaction(USER_ID, 1000, TransactionType.INCOME, CATEGORY_NAME);

    Transaction tx = result.getTransaction();
    assertThat(tx).isNotNull();
    assertThat(tx.getSum()).isEqualTo(1000);
    assertThat(tx.getType()).isEqualTo(TransactionType.INCOME);
    assertThat(tx.getCategory()).isEqualTo(CATEGORY_NAME);
    assertThat(tx.getUserId()).isEqualTo(USER_ID);
    assertThat(tx.getId()).isNotNull();
    assertThat(tx.getDate()).isNotNull();
    assertThat(result.getWarnings()).isEmpty();
  }

  @Test
  void registerTransaction_expense_withoutBudget_warnsIncomeOnly() {
    when(categoryService.findByName(CATEGORY_NAME)).thenReturn(category);

    TransactionResult result =
        service.registerTransaction(USER_ID, 500, TransactionType.EXPENSE, CATEGORY_NAME);

    assertThat(result.getWarnings())
        .extracting(Warning::getType)
        .containsExactly(Warning.Type.EXPENSES_EXCEED_INCOME);
  }

  @Test
  void registerTransaction_expense_lessThanIncome_noWarnings() {
    when(categoryService.findByName(CATEGORY_NAME)).thenReturn(category);

    service.registerTransaction(USER_ID, 1000, TransactionType.INCOME, CATEGORY_NAME);
    TransactionResult result =
        service.registerTransaction(USER_ID, 500, TransactionType.EXPENSE, CATEGORY_NAME);

    assertThat(result.getWarnings()).isEmpty();
  }

  @Test
  void registerTransaction_expense_exceedsIncome_warns() {
    when(categoryService.findByName(CATEGORY_NAME)).thenReturn(category);

    service.registerTransaction(USER_ID, 100, TransactionType.INCOME, CATEGORY_NAME);
    TransactionResult result =
        service.registerTransaction(USER_ID, 500, TransactionType.EXPENSE, CATEGORY_NAME);

    assertThat(result.getWarnings())
        .extracting(Warning::getType)
        .containsExactly(Warning.Type.EXPENSES_EXCEED_INCOME);
  }

  @Test
  void registerTransaction_expense_exceedsBudget_warns() {
    when(categoryService.findByName(CATEGORY_NAME)).thenReturn(category);

    service.registerBudget(USER_ID, 100, CATEGORY_NAME);
    TransactionResult result =
        service.registerTransaction(USER_ID, 500, TransactionType.EXPENSE, CATEGORY_NAME);

    assertThat(result.getWarnings())
        .extracting(Warning::getType)
        .contains(Warning.Type.EXPENSES_EXCEED_BUDGET);
  }

  @Test
  void registerTransaction_expense_equalsBudget_warns() {
    when(categoryService.findByName(CATEGORY_NAME)).thenReturn(category);

    service.registerBudget(USER_ID, 500, CATEGORY_NAME);
    TransactionResult result =
        service.registerTransaction(USER_ID, 500, TransactionType.EXPENSE, CATEGORY_NAME);

    assertThat(result.getWarnings())
        .extracting(Warning::getType)
        .contains(Warning.Type.BUDGET_EXHAUSTED);
  }

  @Test
  void registerTransaction_categoryNotFound_throws() {
    when(categoryService.findByName(anyString()))
        .thenThrow(new CategoryNotFoundException(CATEGORY_NAME));

    assertThatThrownBy(
            () -> service.registerTransaction(USER_ID, 100, TransactionType.INCOME, CATEGORY_NAME))
        .isInstanceOf(CategoryNotFoundException.class);
  }

  @Test
  void registerBudget_success() {
    when(categoryService.findByName(CATEGORY_NAME)).thenReturn(category);

    Budget budget = service.registerBudget(USER_ID, 1000, CATEGORY_NAME);

    assertThat(budget).isNotNull();
    assertThat(budget.getSum()).isEqualTo(1000);
    assertThat(budget.getUserId()).isEqualTo(USER_ID);
    assertThat(budget.getCategory()).isEqualTo(CATEGORY_NAME);
    assertThat(budget.getId()).isNotNull();
  }

  @Test
  void registerBudget_alreadyExists_throws() {
    when(categoryService.findByName(CATEGORY_NAME)).thenReturn(category);

    service.registerBudget(USER_ID, 1000, CATEGORY_NAME);

    assertThatThrownBy(() -> service.registerBudget(USER_ID, 2000, CATEGORY_NAME))
        .isInstanceOf(BudgetAlreadyExistsException.class);
  }

  @Test
  void registerBudget_categoryNotFound_throws() {
    when(categoryService.findByName(anyString()))
        .thenThrow(new CategoryNotFoundException(CATEGORY_NAME));

    assertThatThrownBy(() -> service.registerBudget(USER_ID, 1000, CATEGORY_NAME))
        .isInstanceOf(CategoryNotFoundException.class);
  }

  @Test
  void removeBudget_success() {
    when(categoryService.findByName(CATEGORY_NAME)).thenReturn(category);

    service.registerBudget(USER_ID, 1000, CATEGORY_NAME);
    service.removeBudget(USER_ID, CATEGORY_NAME);

    assertThatThrownBy(() -> service.removeBudget(USER_ID, CATEGORY_NAME))
        .isInstanceOf(BudgetNotFoundException.class);
  }

  @Test
  void removeBudget_notFound_throws() {
    when(categoryService.findByName(CATEGORY_NAME)).thenReturn(category);

    assertThatThrownBy(() -> service.removeBudget(USER_ID, CATEGORY_NAME))
        .isInstanceOf(BudgetNotFoundException.class);
  }

  @Test
  void removeBudget_categoryNotFound_throws() {
    when(categoryService.findByName(anyString()))
        .thenThrow(new CategoryNotFoundException(CATEGORY_NAME));

    assertThatThrownBy(() -> service.removeBudget(USER_ID, CATEGORY_NAME))
        .isInstanceOf(CategoryNotFoundException.class);
  }

  @Test
  void findTransactions_existingCategory_returnsData() {
    when(categoryService.findByName(CATEGORY_NAME)).thenReturn(category);

    service.registerTransaction(USER_ID, 100, TransactionType.EXPENSE, CATEGORY_NAME);

    TransactionData data = service.findTransactionsByUserIdAndCategory(USER_ID, CATEGORY_NAME);

    assertThat(data).isNotNull();
    assertThat(data.getCategory()).isEqualTo(CATEGORY_NAME);
    assertThat(data.getTransactions()).hasSize(1);
    assertThat(data.getExpenseSum()).isEqualTo(100);
  }

  @Test
  void findTransactions_summaryExistsButCategoryEmpty_returnsEmptyData() {
    when(categoryService.findByName(CATEGORY_NAME)).thenReturn(category);

    Category other = new Category(UUID.randomUUID(), "ДРУГАЯ", USER_ID, LocalDate.now());
    when(categoryService.findByName("ДРУГАЯ")).thenReturn(other);
    service.registerTransaction(USER_ID, 100, TransactionType.INCOME, "ДРУГАЯ");

    TransactionData data = service.findTransactionsByUserIdAndCategory(USER_ID, CATEGORY_NAME);

    assertThat(data).isNotNull();
    assertThat(data.getCategory()).isEqualTo(CATEGORY_NAME);
    assertThat(data.getTransactions()).isEmpty();
    assertThat(data.getIncomeSum()).isEqualTo(0);
    assertThat(data.getExpenseSum()).isEqualTo(0);
  }

  @Test
  void findTransactions_userNotFound_throws() {
    when(categoryService.findByName(CATEGORY_NAME)).thenReturn(category);

    assertThatThrownBy(
            () -> service.findTransactionsByUserIdAndCategory(UUID.randomUUID(), CATEGORY_NAME))
        .isInstanceOf(FinancialSummaryNotFoundException.class);
  }

  @Test
  void findFinancialSummary_existingUser_returnsSummary() {
    when(categoryService.findByName(CATEGORY_NAME)).thenReturn(category);
    service.registerTransaction(USER_ID, 100, TransactionType.INCOME, CATEGORY_NAME);

    FinancialSummary summary = service.findFinancialSummaryByUserId(USER_ID);

    assertThat(summary).isNotNull();
    assertThat(summary.getUserId()).isEqualTo(USER_ID);
    assertThat(summary.getIncomeSum()).isEqualTo(100);
  }

  @Test
  void findFinancialSummary_userNotFound_throws() {
    assertThatThrownBy(() -> service.findFinancialSummaryByUserId(UUID.randomUUID()))
        .isInstanceOf(FinancialSummaryNotFoundException.class);
  }
}

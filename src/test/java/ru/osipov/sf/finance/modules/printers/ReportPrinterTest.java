package ru.osipov.sf.finance.modules.printers;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.osipov.sf.finance.models.aggregate.FinancialSummary;
import ru.osipov.sf.finance.models.aggregate.TransactionData;
import ru.osipov.sf.finance.models.entity.Budget;
import ru.osipov.sf.finance.models.entity.Category;
import ru.osipov.sf.finance.models.entity.Transaction;
import ru.osipov.sf.finance.models.enums.TransactionType;

class ReportPrinterTest {

  private static final UUID USER_ID = UUID.randomUUID();
  private static final String CATEGORY_NAME = "ЕДА";

  private ByteArrayOutputStream outContent;
  private PrintStream originalOut;

  @BeforeEach
  void setUp() {
    originalOut = System.out;
    outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));
  }

  @AfterEach
  void tearDown() {
    System.setOut(originalOut);
  }

  private Category newCategory() {
    return new Category(UUID.randomUUID(), CATEGORY_NAME, USER_ID, LocalDate.now());
  }

  private Transaction newTransaction(Category category, int sum, TransactionType type) {
    return new Transaction(UUID.randomUUID(), USER_ID, sum, type, category, LocalDate.now());
  }

  @Test
  void printShortReport_profit_printsProfit() {
    Category category = newCategory();
    FinancialSummary summary = new FinancialSummary(USER_ID);
    summary.addTransaction(category, newTransaction(category, 1000, TransactionType.INCOME));
    summary.addTransaction(category, newTransaction(category, 300, TransactionType.EXPENSE));

    ReportPrinter.printShortReport(summary);

    String output = outContent.toString();
    assertThat(output).contains("Краткий отчёт");
    assertThat(output).contains("Сумма доходов:    1000");
    assertThat(output).contains("Сумма расходов:   300");
    assertThat(output).contains("Профицит:         700");
    assertThat(output).doesNotContain("Дефицит");
  }

  @Test
  void printShortReport_deficit_printsDeficit() {
    Category category = newCategory();
    FinancialSummary summary = new FinancialSummary(USER_ID);
    summary.addTransaction(category, newTransaction(category, 100, TransactionType.INCOME));
    summary.addTransaction(category, newTransaction(category, 500, TransactionType.EXPENSE));

    ReportPrinter.printShortReport(summary);

    String output = outContent.toString();
    assertThat(output).contains("Дефицит:          400");
    assertThat(output).doesNotContain("Профицит");
  }

  @Test
  void printShortReport_zeroBalance_printsZeroBalance() {
    Category category = newCategory();
    FinancialSummary summary = new FinancialSummary(USER_ID);
    summary.addTransaction(category, newTransaction(category, 500, TransactionType.INCOME));
    summary.addTransaction(category, newTransaction(category, 500, TransactionType.EXPENSE));

    ReportPrinter.printShortReport(summary);

    String output = outContent.toString();
    assertThat(output).contains("Баланс:           0 (доходы = расходы)");
    assertThat(output).doesNotContain("Профицит");
    assertThat(output).doesNotContain("Дефицит");
  }

  @Test
  void printReportByCategory_withBudgetOverrun_printsOverrun() {
    Category category = newCategory();
    TransactionData txData = new TransactionData(category);
    txData.setBudget(new Budget(UUID.randomUUID(), USER_ID, 100, category));
    txData.addTransaction(newTransaction(category, 500, TransactionType.EXPENSE));

    ReportPrinter.printReportByCategory(txData);

    String output = outContent.toString();
    assertThat(output).contains("Отчёт по категории");
    assertThat(output).contains("Категория:        " + CATEGORY_NAME);
    assertThat(output).contains("Расходы:          500");
    assertThat(output).contains("Бюджет:           100");
    assertThat(output).contains("Перерасход:       400");
  }

  @Test
  void printReportByCategory_withoutBudget_printsNotSet() {
    Category category = newCategory();
    TransactionData txData = new TransactionData(category);

    ReportPrinter.printReportByCategory(txData);

    String output = outContent.toString();
    assertThat(output).contains("Бюджет:           <не установлен>");
    assertThat(output).contains("(нет транзакций)");
  }

  @Test
  void printFullReport_emptySummary_printsNoTransactions() {
    FinancialSummary summary = new FinancialSummary(USER_ID);

    ReportPrinter.printFullReport(summary);

    String output = outContent.toString();
    assertThat(output).contains("Подробный отчёт");
    assertThat(output).contains("Сумма всех доходов:   0");
    assertThat(output).contains("Сумма всех расходов:  0");
    assertThat(output).contains("Транзакций нет.");
  }
}

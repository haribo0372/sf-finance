package ru.osipov.sf.finance.modules.printers;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.osipov.sf.finance.models.warning.Warning;

class WarningPrinterTest {

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

  @Test
  void printWarnings_emptyList_printsNothing() {
    WarningPrinter.printWarnings(List.of());

    assertThat(outContent.toString()).isEmpty();
  }

  @Test
  void printWarnings_exceedsIncome_printsWarning() {
    WarningPrinter.printWarnings(List.of(Warning.expensesExceedIncome(100, 500)));

    String output = outContent.toString();
    assertThat(output).contains("ВНИМАНИЕ: расходы превышают доходы");
    assertThat(output).contains("Сумма доходов:    100");
    assertThat(output).contains("Сумма расходов:   500");
    assertThat(output).contains("Дефицит:          400");
    assertThat(output).doesNotContain("превышение бюджета");
    assertThat(output).doesNotContain("бюджет исчерпан");
  }

  @Test
  void printWarnings_exceedsBudget_printsWarning() {
    WarningPrinter.printWarnings(List.of(Warning.expensesExceedBudget("ЕДА", 500, 100)));

    String output = outContent.toString();
    assertThat(output).contains("ВНИМАНИЕ: превышение бюджета");
    assertThat(output).contains("Категория:        ЕДА");
    assertThat(output).contains("Бюджет:           100");
    assertThat(output).contains("Сумма расходов:   500");
    assertThat(output).contains("Превышение:       400");
    assertThat(output).doesNotContain("расходы превышают доходы");
    assertThat(output).doesNotContain("бюджет исчерпан");
  }

  @Test
  void printWarnings_budgetExhausted_printsWarning() {
    WarningPrinter.printWarnings(List.of(Warning.budgetExhausted("ЕДА", 500, 500)));

    String output = outContent.toString();
    assertThat(output).contains("Бюджет исчерпан");
    assertThat(output).contains("Категория:        ЕДА");
    assertThat(output).contains("Бюджет:           500");
    assertThat(output).contains("Сумма расходов:   500");
    assertThat(output).doesNotContain("расходы превышают доходы");
    assertThat(output).doesNotContain("превышение бюджета");
  }

  @Test
  void printWarnings_multipleWarnings_printsAll() {
    WarningPrinter.printWarnings(
        List.of(
            Warning.expensesExceedIncome(100, 500), Warning.expensesExceedBudget("ЕДА", 500, 100)));

    String output = outContent.toString();
    assertThat(output).contains("расходы превышают доходы");
    assertThat(output).contains("превышение бюджета");
  }
}

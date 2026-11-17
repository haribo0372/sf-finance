package ru.osipov.sf.finance.modules.printers;

import java.util.List;
import ru.osipov.sf.finance.models.warning.Warning;

/** Печатает уведомления о превышении доходов или бюджета в консоль. */
public final class WarningPrinter {

  /**
   * Печатает список уведомлений. Для каждого уведомления выводит соответствующее сообщение в
   * зависимости от его типа.
   *
   * @param warnings список уведомлений (может быть пустым — тогда ничего не печатается)
   */
  public static void printWarnings(List<Warning> warnings) {
    for (Warning w : warnings) {
      switch (w.getType()) {
        case EXPENSES_EXCEED_INCOME -> printExceedsIncome(w);
        case EXPENSES_EXCEED_BUDGET -> printExceedsBudget(w);
        case BUDGET_EXHAUSTED -> printBudgetExhausted(w);
      }
    }
  }

  /**
   * Печатает уведомление о превышении расходов над доходами: суммы доходов/расходов и дефицит.
   *
   * @param w уведомление типа {@link Warning.Type#EXPENSES_EXCEED_INCOME}
   */
  private static void printExceedsIncome(Warning w) {
    int deficit = w.getExpenseSum() - w.getIncomeSum();
    System.out.println();
    System.out.println("!!! ВНИМАНИЕ: расходы превышают доходы !!!");
    System.out.println("-----------------------------------------");
    System.out.printf("Сумма доходов:    %s%n", w.getIncomeSum());
    System.out.printf("Сумма расходов:   %s%n", w.getExpenseSum());
    System.out.printf("Дефицит:          %s%n", deficit);
    System.out.println("=========================================");
  }

  /**
   * Печатает уведомление о превышении расходов над бюджетом: категорию, бюджет, расходы и размер
   * превышения.
   *
   * @param w уведомление типа {@link Warning.Type#EXPENSES_EXCEED_BUDGET}
   */
  private static void printExceedsBudget(Warning w) {
    int overrun = w.getExpenseSum() - w.getBudgetSum();
    System.out.println();
    System.out.println("!!! ВНИМАНИЕ: превышение бюджета !!!");
    System.out.println("----------------------------------");
    System.out.printf("Категория:        %s%n", w.getCategory());
    System.out.printf("Бюджет:           %s%n", w.getBudgetSum());
    System.out.printf("Сумма расходов:   %s%n", w.getExpenseSum());
    System.out.printf("Превышение:       %s%n", overrun);
    System.out.println("==================================");
  }

  /**
   * Печатает уведомление о том, что бюджет исчерпан (расходы равны бюджету): категорию, бюджет и
   * сумму расходов.
   *
   * @param w уведомление типа {@link Warning.Type#BUDGET_EXHAUSTED}
   */
  private static void printBudgetExhausted(Warning w) {
    System.out.println();
    System.out.println("Бюджет исчерпан");
    System.out.println("----------------------------------");
    System.out.printf("Категория:        %s%n", w.getCategory());
    System.out.printf("Бюджет:           %s%n", w.getBudgetSum());
    System.out.printf("Сумма расходов:   %s%n", w.getExpenseSum());
    System.out.println("==================================");
  }
}

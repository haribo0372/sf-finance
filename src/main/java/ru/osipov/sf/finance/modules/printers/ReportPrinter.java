package ru.osipov.sf.finance.modules.printers;

import ru.osipov.sf.finance.models.aggregate.FinancialSummary;
import ru.osipov.sf.finance.models.aggregate.TransactionData;
import ru.osipov.sf.finance.models.entity.Budget;

/** Печатает отчёты по транзакциям и бюджетам в консоль. */
public final class ReportPrinter {

  /**
   * Печатает отчёт по одной категории: доходы, расходы, баланс, бюджет (если установлен) и список
   * транзакций.
   *
   * @param txData данные по категории (транзакции, суммы, бюджет)
   */
  public static void printReportByCategory(TransactionData txData) {
    Budget budget = txData.getBudget();
    int income = txData.getIncomeSum();
    int expense = txData.getExpenseSum();

    System.out.println();
    System.out.println("=== Отчёт по категории ===");
    System.out.printf("Категория:        %s%n", txData.getCategory());
    System.out.println("--------------------------");
    System.out.printf("Доходы:           %s%n", income);
    System.out.printf("Расходы:          %s%n", expense);
    System.out.printf("Баланс категории: %s%n", income - expense);

    if (budget != null) {
      int budgetSum = budget.getSum();
      int budgetBalance = budgetSum - expense;
      System.out.println("--------------------------");
      System.out.printf("Бюджет:           %s%n", budgetSum);
      if (budgetBalance >= 0) {
        System.out.printf("Остаток бюджета:  %s%n", budgetBalance);
      } else {
        System.out.printf("Перерасход:       %s%n", -budgetBalance);
      }
      System.out.printf("Итог (бюджет+доходы-расходы): %s%n", budgetSum + income - expense);
    } else {
      System.out.println("--------------------------");
      System.out.println("Бюджет:           <не установлен>");
    }

    System.out.println("--------------------------");
    if (txData.getTransactions().isEmpty()) {
      System.out.println("    (нет транзакций)");
      System.out.println("==========================");
      return;
    }

    System.out.println("Транзакции:");
    for (var tx : txData.getTransactions()) {
      System.out.printf(
          "    - Дата: %s | Тип: %-7s | Сумма: %s%n",
          tx.getDate(), tx.getType().getLabel(), tx.getSum());
    }
    System.out.println("==========================");
  }

  /**
   * Печатает краткий отчёт по пользователю: суммы доходов и расходов, а также итоговый баланс
   * (профицит, дефицит или ноль).
   *
   * @param summary финансовая сводка пользователя
   */
  public static void printShortReport(FinancialSummary summary) {
    int income = summary.getIncomeSum();
    int expense = summary.getExpenseSum();
    int balance = income - expense;

    System.out.println();
    System.out.println("=== Краткий отчёт ===");
    System.out.printf("Сумма доходов:    %s%n", income);
    System.out.printf("Сумма расходов:   %s%n", expense);
    System.out.println("--------------------");
    if (balance > 0) {
      System.out.printf("Профицит:         %s%n", balance);
    } else if (balance < 0) {
      System.out.printf("Дефицит:          %s%n", -balance);
    } else {
      System.out.println("Баланс:           0 (доходы = расходы)");
    }
    System.out.println("====================");
  }

  /**
   * Печатает подробный отчёт по пользователю: общие суммы доходов и расходов, а затем по каждой
   * категории — доходы, расходы, баланс, бюджет (если установлен) и список транзакций.
   *
   * @param summary финансовая сводка пользователя
   */
  public static void printFullReport(FinancialSummary summary) {
    System.out.println();
    System.out.println("=== Подробный отчёт ===");
    System.out.printf("Сумма всех доходов:   %s%n", summary.getIncomeSum());
    System.out.printf("Сумма всех расходов:  %s%n", summary.getExpenseSum());
    System.out.println("=======================");

    if (summary.getTransactionsByCategory().isEmpty()) {
      System.out.println("Транзакций нет.");
      return;
    }

    for (var entry : summary.getTransactionsByCategory().entrySet()) {
      String category = entry.getKey();
      TransactionData txData = entry.getValue();
      Budget budget = txData.getBudget();
      int income = txData.getIncomeSum();
      int expense = txData.getExpenseSum();

      System.out.println();
      System.out.printf("Категория: %s%n", category);
      System.out.printf("  Доходы:            %s%n", income);
      System.out.printf("  Расходы:           %s%n", expense);
      System.out.printf("  Баланс категории:  %s%n", income - expense);

      if (budget != null) {
        int budgetSum = budget.getSum();
        int budgetBalance = budgetSum - expense;
        System.out.printf("  Бюджет:            %s%n", budgetSum);
        if (budgetBalance >= 0) {
          System.out.printf("  Остаток бюджета:   %s%n", budgetBalance);
        } else {
          System.out.printf("  Перерасход:        %s%n", -budgetBalance);
        }
        System.out.printf("  Итог (бюджет+доходы-расходы): %s%n", budgetSum + income - expense);
      } else {
        System.out.println("  Бюджет:            <не установлен>");
      }

      System.out.println("  Транзакции:");
      if (txData.getTransactions().isEmpty()) {
        System.out.println("    (нет)");
        continue;
      }
      for (var tx : txData.getTransactions()) {
        System.out.printf(
            "    - Дата: %s | Тип: %-7s | Сумма: %s%n",
            tx.getDate(), tx.getType().getLabel(), tx.getSum());
      }
    }
    System.out.println("=======================");
  }
}

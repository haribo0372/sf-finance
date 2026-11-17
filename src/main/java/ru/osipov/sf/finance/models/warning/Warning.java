package ru.osipov.sf.finance.models.warning;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Уведомление о превышении: расходов над доходами, расходов над бюджетом или исчерпании бюджета.
 *
 * <p>Создаётся через фабричные методы {@link #expensesExceedIncome}, {@link #expensesExceedBudget}
 * или {@link #budgetExhausted} — они заполняют только те поля, которые важны для конкретного типа.
 */
@Getter
@RequiredArgsConstructor
public class Warning {

  /** Тип уведомления. */
  public enum Type {
    /** Расходы превышают доходы (дефицит). */
    EXPENSES_EXCEED_INCOME,

    /** Расходы превышают установленный бюджет категории (перерасход). */
    EXPENSES_EXCEED_BUDGET,

    /** Расходы равны бюджету (бюджет исчерпан). */
    BUDGET_EXHAUSTED
  }

  private final Type type;
  private final String category;
  private final int incomeSum;
  private final int expenseSum;
  private final int budgetSum;

  /**
   * Создаёт уведомление о превышении расходов над доходами.
   *
   * @param incomeSum сумма доходов
   * @param expenseSum сумма расходов
   * @return уведомление типа {@link Type#EXPENSES_EXCEED_INCOME}
   */
  public static Warning expensesExceedIncome(int incomeSum, int expenseSum) {
    return new Warning(Type.EXPENSES_EXCEED_INCOME, null, incomeSum, expenseSum, 0);
  }

  /**
   * Создаёт уведомление о превышении расходов над бюджетом категории.
   *
   * @param category имя категории
   * @param expenseSum сумма расходов
   * @param budgetSum установленный бюджет
   * @return уведомление типа {@link Type#EXPENSES_EXCEED_BUDGET}
   */
  public static Warning expensesExceedBudget(String category, int expenseSum, int budgetSum) {
    return new Warning(Type.EXPENSES_EXCEED_BUDGET, category, 0, expenseSum, budgetSum);
  }

  /**
   * Создаёт уведомление о том, что бюджет исчерпан (расходы равны бюджету).
   *
   * @param category имя категории
   * @param expenseSum сумма расходов
   * @param budgetSum установленный бюджет
   * @return уведомление типа {@link Type#BUDGET_EXHAUSTED}
   */
  public static Warning budgetExhausted(String category, int expenseSum, int budgetSum) {
    return new Warning(Type.BUDGET_EXHAUSTED, category, 0, expenseSum, budgetSum);
  }
}

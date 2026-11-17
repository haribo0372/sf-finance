package ru.osipov.sf.finance.models.aggregate;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.osipov.sf.finance.models.entity.Budget;
import ru.osipov.sf.finance.models.entity.Category;
import ru.osipov.sf.finance.models.entity.Transaction;

/**
 * Данные по одной категории в рамках финансовой сводки пользователя: суммы доходов и расходов,
 * установленный бюджет и список транзакций.
 *
 * <p>При добавлении транзакции автоматически обновляются суммы доходов/расходов и список
 * транзакций.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionData {

  private String category;
  private Budget budget;
  private Integer incomeSum;
  private Integer expenseSum;
  private List<Transaction> transactions;

  /**
   * Создаёт пустые данные для категории: суммы равны нулю, бюджет не установлен, транзакций нет.
   *
   * @param category категория
   */
  public TransactionData(Category category) {
    this.category = category.getName();
    this.incomeSum = 0;
    this.expenseSum = 0;
    this.transactions = new ArrayList<>();
  }

  /**
   * Добавляет транзакцию: обновляет сумму доходов или расходов и добавляет транзакцию в список.
   *
   * @param transaction транзакция для добавления
   * @throws IllegalArgumentException если тип транзакции неизвестен
   */
  public void addTransaction(Transaction transaction) {
    switch (transaction.getType()) {
      case INCOME -> incomeSum += transaction.getSum();
      case EXPENSE -> expenseSum += transaction.getSum();
      default ->
          throw new IllegalArgumentException("Unknown transaction type: " + transaction.getType());
    }

    transactions.add(transaction);
  }
}

package ru.osipov.sf.finance.models.aggregate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.osipov.sf.finance.models.entity.Category;
import ru.osipov.sf.finance.models.entity.Transaction;

/**
 * Финансовая сводка пользователя: суммарные доходы и расходы, а также данные по категориям.
 *
 * <p>Сводка агрегирует все транзакции пользователя и группирует их по категориям через {@link
 * TransactionData}. При добавлении транзакции автоматически обновляются суммарные доходы/расходы и
 * данные соответствующей категории.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinancialSummary {

  private UUID userId;
  private Integer incomeSum;
  private Integer expenseSum;
  private Map<String, TransactionData> transactionsByCategory;

  /**
   * Создаёт пустую сводку для пользователя: суммы равны нулю, категорий нет.
   *
   * @param userId идентификатор пользователя
   */
  public FinancialSummary(UUID userId) {
    this.userId = userId;
    this.incomeSum = 0;
    this.expenseSum = 0;
    this.transactionsByCategory = new HashMap<>();
  }

  /**
   * Добавляет транзакцию в сводку: обновляет суммарные доходы или расходы и добавляет транзакцию в
   * данные соответствующей категории (создавая их при необходимости).
   *
   * @param category категория транзакции
   * @param transaction транзакция для добавления
   * @throws IllegalArgumentException если тип транзакции неизвестен
   */
  public void addTransaction(Category category, Transaction transaction) {
    switch (transaction.getType()) {
      case INCOME -> incomeSum += transaction.getSum();
      case EXPENSE -> expenseSum += transaction.getSum();
      default ->
          throw new IllegalArgumentException("Unknown transaction type: " + transaction.getType());
    }

    transactionsByCategory
        .computeIfAbsent(category.getName(), k -> new TransactionData(category))
        .addTransaction(transaction);
  }

  /**
   * Добавляет готовые данные по категории в сводку. Если данные для этой категории уже есть — они
   * заменяются.
   *
   * @param txData данные по категории
   */
  public void addTransactionData(TransactionData txData) {
    transactionsByCategory.put(txData.getCategory(), txData);
  }

  /**
   * Возвращает данные по указанной категории.
   *
   * @param category категория
   * @return данные по категории или {@link Optional#empty()}, если транзакций в ней нет
   */
  public Optional<TransactionData> getTransactionDataByCategory(Category category) {
    return Optional.ofNullable(transactionsByCategory.get(category.getName()));
  }
}

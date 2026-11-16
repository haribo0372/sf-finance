package ru.osipov.sf.finance.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.osipov.sf.finance.models.entity.Category;
import ru.osipov.sf.finance.models.entity.Transaction;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinancialSummary {

  private UUID userId;
  private Integer incomeSum;
  private Integer expenseSum;
  private Map<String, TransactionData> transactionsByCategory;

  public FinancialSummary(UUID userId) {
    this.userId = userId;
    this.incomeSum = 0;
    this.expenseSum = 0;
    this.transactionsByCategory = new HashMap<>();
  }

  public void addTransaction(Transaction transaction) {
    switch (transaction.getType()) {
      case INCOME -> incomeSum += transaction.getSum();
      case EXPENSE -> expenseSum += transaction.getSum();
      default ->
          throw new IllegalArgumentException("Unknown transaction type: " + transaction.getType());
    }

    transactionsByCategory
        .computeIfAbsent(
            transaction.getCategory(), k -> new TransactionData(transaction.getCategory()))
        .addTransaction(transaction);
  }

  public void addTransactionData(TransactionData txData) {
    transactionsByCategory.put(txData.getCategory(), txData);
  }

  public Optional<TransactionData> getTransactionDataByCategory(Category category) {
    return Optional.ofNullable(transactionsByCategory.get(category.getName()));
  }
}

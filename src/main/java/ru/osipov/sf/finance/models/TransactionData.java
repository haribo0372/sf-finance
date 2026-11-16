package ru.osipov.sf.finance.models;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.osipov.sf.finance.models.entity.Budget;
import ru.osipov.sf.finance.models.entity.Category;
import ru.osipov.sf.finance.models.entity.Transaction;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionData {

  private String category;
  private Budget budget;
  private Integer incomeSum;
  private Integer expenseSum;
  private List<Transaction> transactions;

  public TransactionData(Category category) {
    this.category = category.getName();
    this.incomeSum = 0;
    this.expenseSum = 0;
    this.transactions = new ArrayList<>();
  }

  public TransactionData(String category) {
    this.category = category;
    this.incomeSum = 0;
    this.expenseSum = 0;
    this.transactions = new ArrayList<>();
  }

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

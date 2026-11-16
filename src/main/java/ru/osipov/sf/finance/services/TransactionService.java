package ru.osipov.sf.finance.services;

import java.util.UUID;

import ru.osipov.sf.finance.models.FinancialSummary;
import ru.osipov.sf.finance.models.TransactionData;
import ru.osipov.sf.finance.models.entity.Budget;
import ru.osipov.sf.finance.models.entity.Transaction;
import ru.osipov.sf.finance.models.enums.TransactionType;

public interface TransactionService {

  Transaction registerTransaction(
      UUID userId, Integer sum, TransactionType type, String categoryName);

  Budget registerBudget(UUID userId, Integer sum, String categoryName);

  void removeBudget(UUID userId, String categoryName);

  TransactionData findTransactionsByUserIdAndCategory(UUID userId, String categoryName);

  FinancialSummary findFinancialSummaryByUserId(UUID userId);
}

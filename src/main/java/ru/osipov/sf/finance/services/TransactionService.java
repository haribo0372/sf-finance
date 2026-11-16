package ru.osipov.sf.finance.services;

import java.util.UUID;
import ru.osipov.sf.finance.models.entity.Transaction;
import ru.osipov.sf.finance.models.enums.TransactionType;

public interface TransactionService {

  Transaction save(UUID userId, Integer sum, TransactionType type, String categoryName);
}

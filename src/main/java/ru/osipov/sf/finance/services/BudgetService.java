package ru.osipov.sf.finance.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import ru.osipov.sf.finance.models.entity.Budget;

public interface BudgetService {

  Budget save(UUID userId, Integer sum, String categoryName);

  Optional<Budget> findByUserIdAndCategory(UUID userId, String categoryName);

  List<Budget> findByUserId(UUID userId);
}

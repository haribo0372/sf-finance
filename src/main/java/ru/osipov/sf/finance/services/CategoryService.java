package ru.osipov.sf.finance.services;

import java.util.Set;
import java.util.UUID;
import ru.osipov.sf.finance.models.entity.Category;

public interface CategoryService {

  Category save(UUID userId, String name);

  Category findByName(String name);

  Set<String> getAllCategoryNames();
}

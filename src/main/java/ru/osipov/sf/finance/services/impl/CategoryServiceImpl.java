package ru.osipov.sf.finance.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import ru.osipov.sf.finance.config.ApplicationConfig;
import ru.osipov.sf.finance.exception.CategoryAlreadyExistsException;
import ru.osipov.sf.finance.exception.CategoryNotFoundException;
import ru.osipov.sf.finance.models.entity.Category;
import ru.osipov.sf.finance.services.AbstractEntityService;
import ru.osipov.sf.finance.services.CategoryService;

public class CategoryServiceImpl extends AbstractEntityService<Category>
    implements CategoryService {

  public CategoryServiceImpl(ApplicationConfig applicationConfig, ObjectMapper mapper) {
    super(
        applicationConfig.getCategoryFile(), mapper, new TypeReference<Map<String, Category>>() {});
  }

  @Override
  public Category save(UUID userId, String name) {
    String nameUpperCase = name.toUpperCase();
    Category category = storage.get(nameUpperCase);

    if (category != null) throw new CategoryAlreadyExistsException(nameUpperCase);

    category = new Category(UUID.randomUUID(), nameUpperCase, userId, LocalDate.now());
    storage.put(nameUpperCase, category);
    saveToFile();
    return category;
  }

  @Override
  public Category findByName(String name) {
    var upperName = name.toUpperCase();
    Category category = storage.get(upperName);
    if (category == null) throw new CategoryNotFoundException(upperName);
    return category;
  }

  @Override
  public Set<String> getAllCategoryNames() {
    return storage.keySet();
  }
}

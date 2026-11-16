package ru.osipov.sf.finance.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import ru.osipov.sf.finance.config.ApplicationConfig;
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
    String message;

    if (category == null) {
      category = new Category(UUID.randomUUID(), nameUpperCase, userId, LocalDate.now());
      storage.put(nameUpperCase, category);
      saveToFile();
      message = "Категория успешно создана: ";
    } else {
      message = "Категория уже существует: ";
    }

    System.out.println(message + category);
    return category;
  }

  @Override
  public Optional<Category> findByName(String name) {
    return Optional.ofNullable(storage.get(name.toUpperCase()));
  }

  @Override
  public Set<String> getAllCategoryNames() {
    return storage.keySet();
  }
}

package ru.osipov.sf.finance.services;

import java.util.Set;
import java.util.UUID;
import ru.osipov.sf.finance.models.entity.Category;

/** Сервис категорий: создание, поиск и список имён. Категории глобальные (общие для всех). */
public interface CategoryService {

  /**
   * Создаёт новую категорию.
   *
   * @param userId идентификатор создателя
   * @param name имя категории (нормализуется в верхний регистр)
   * @return созданная категория
   * @throws ru.osipov.sf.finance.exception.out.CategoryAlreadyExistsException если категория с
   *     таким именем уже существует
   */
  Category save(UUID userId, String name);

  /**
   * Находит категорию по имени (без учёта регистра).
   *
   * @param name имя категории
   * @return найденная категория
   * @throws ru.osipov.sf.finance.exception.out.CategoryNotFoundException если категория не найдена
   */
  Category findByName(String name);

  /**
   * Возвращает имена всех существующих категорий.
   *
   * @return множество имён категорий (в верхнем регистре)
   */
  Set<String> getAllCategoryNames();
}

package ru.osipov.sf.finance.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.osipov.sf.finance.config.ApplicationConfig;
import ru.osipov.sf.finance.config.JsonConfig;
import ru.osipov.sf.finance.exception.out.CategoryAlreadyExistsException;
import ru.osipov.sf.finance.exception.out.CategoryNotFoundException;
import ru.osipov.sf.finance.models.entity.Category;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

  private static final UUID USER_ID = UUID.randomUUID();
  private static final UUID OTHER_USER_ID = UUID.randomUUID();

  @TempDir Path tempDir;

  @Mock private ApplicationConfig applicationConfig;

  private CategoryServiceImpl service;

  @BeforeEach
  void setUp() {
    when(applicationConfig.getCategoryFile())
        .thenReturn(tempDir.resolve("categories.json").toString());

    JsonConfig jsonConfig = new JsonConfig();
    service = new CategoryServiceImpl(applicationConfig, jsonConfig.getMapper());
  }

  @Test
  void save_newCategory_success() {
    Category category = service.save(USER_ID, "Еда");

    assertThat(category).isNotNull();
    assertThat(category.getId()).isNotNull();
    assertThat(category.getName()).isEqualTo("ЕДА");
    assertThat(category.getCreatedBy()).isEqualTo(USER_ID);
    assertThat(category.getCreatedAt()).isNotNull();
  }

  @Test
  void save_normalizesNameToUpperCase() {
    Category category = service.save(USER_ID, "еда");

    assertThat(category.getName()).isEqualTo("ЕДА");
  }

  @Test
  void save_trimsAndNormalizesMixedCase() {
    Category category = service.save(USER_ID, "Еда");

    assertThat(category.getName()).isEqualTo("ЕДА");
  }

  @Test
  void save_duplicateSameCase_throws() {
    service.save(USER_ID, "Еда");

    assertThatThrownBy(() -> service.save(USER_ID, "Еда"))
        .isInstanceOf(CategoryAlreadyExistsException.class)
        .hasMessageContaining("ЕДА");
  }

  @Test
  void save_duplicateDifferentCase_throws() {
    service.save(USER_ID, "Еда");

    assertThatThrownBy(() -> service.save(USER_ID, "еда"))
        .isInstanceOf(CategoryAlreadyExistsException.class)
        .hasMessageContaining("ЕДА");
  }

  @Test
  void save_duplicateByAnotherUser_throws() {
    service.save(USER_ID, "Еда");

    assertThatThrownBy(() -> service.save(OTHER_USER_ID, "Еда"))
        .isInstanceOf(CategoryAlreadyExistsException.class)
        .hasMessageContaining("ЕДА");
  }

  @Test
  void save_multipleCategories_success() {
    service.save(USER_ID, "Еда");
    service.save(USER_ID, "Транспорт");
    service.save(USER_ID, "Развлечения");

    assertThat(service.getAllCategoryNames())
        .containsExactlyInAnyOrder("ЕДА", "ТРАНСПОРТ", "РАЗВЛЕЧЕНИЯ");
  }

  @Test
  void findByName_existing_returnsCategory() {
    Category saved = service.save(USER_ID, "Еда");

    Category found = service.findByName("Еда");

    assertThat(found).isEqualTo(saved);
    assertThat(found.getName()).isEqualTo("ЕДА");
  }

  @Test
  void findByName_caseInsensitive() {
    service.save(USER_ID, "Еда");

    Category found = service.findByName("еда");

    assertThat(found).isNotNull();
    assertThat(found.getName()).isEqualTo("ЕДА");
  }

  @Test
  void findByName_notFound_throws() {
    assertThatThrownBy(() -> service.findByName("Несуществующая"))
        .isInstanceOf(CategoryNotFoundException.class)
        .hasMessageContaining("НЕСУЩЕСТВУЮЩАЯ");
  }

  @Test
  void findByName_notFoundInEmptyStorage_throws() {
    assertThatThrownBy(() -> service.findByName("Еда"))
        .isInstanceOf(CategoryNotFoundException.class);
  }

  @Test
  void getAllCategoryNames_empty_returnsEmptySet() {
    assertThat(service.getAllCategoryNames()).isEmpty();
  }

  @Test
  void getAllCategoryNames_multiple_returnsAllNames() {
    service.save(USER_ID, "Еда");
    service.save(USER_ID, "Транспорт");

    assertThat(service.getAllCategoryNames()).containsExactlyInAnyOrder("ЕДА", "ТРАНСПОРТ");
  }

  @Test
  void getAllCategoryNames_returnsUpperCaseNames() {
    service.save(USER_ID, "еда");
    service.save(USER_ID, "транспорт");

    assertThat(service.getAllCategoryNames()).containsExactlyInAnyOrder("ЕДА", "ТРАНСПОРТ");
  }

  @Test
  void getAllCategoryNames_reflectsNewCategories() {
    service.save(USER_ID, "Еда");

    assertThat(service.getAllCategoryNames()).containsExactly("ЕДА");

    service.save(USER_ID, "Транспорт");

    assertThat(service.getAllCategoryNames()).containsExactlyInAnyOrder("ЕДА", "ТРАНСПОРТ");
  }
}

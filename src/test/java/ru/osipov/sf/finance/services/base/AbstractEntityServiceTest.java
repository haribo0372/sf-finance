package ru.osipov.sf.finance.services.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
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
import ru.osipov.sf.finance.models.entity.Category;
import ru.osipov.sf.finance.services.impl.CategoryServiceImpl;

@ExtendWith(MockitoExtension.class)
class AbstractEntityServiceTest {

  private static final UUID USER_ID = UUID.randomUUID();

  @TempDir Path tempDir;

  @Mock private ApplicationConfig applicationConfig;

  private Path categoriesFile;
  private JsonConfig jsonConfig;

  @BeforeEach
  void setUp() {
    categoriesFile = tempDir.resolve("categories.json");
    when(applicationConfig.getCategoryFile()).thenReturn(categoriesFile.toString());
    jsonConfig = new JsonConfig();
  }

  private CategoryServiceImpl newService() {
    return new CategoryServiceImpl(applicationConfig, jsonConfig.getMapper());
  }

  @Test
  void saveToFile_createsFile() {
    CategoryServiceImpl service = newService();

    service.save(USER_ID, "Еда");

    assertThat(categoriesFile).exists();
  }

  @Test
  void loadFromFile_roundTripViaFile() {
    CategoryServiceImpl service = newService();
    service.save(USER_ID, "Еда");

    CategoryServiceImpl reloaded = newService();
    Category loaded = reloaded.findByName("Еда");

    assertThat(loaded.getName()).isEqualTo("ЕДА");
    assertThat(loaded.getCreatedBy()).isEqualTo(USER_ID);
  }

  @Test
  void loadFromFile_invalidJson_doesNotThrow() throws IOException {
    Files.writeString(categoriesFile, "{ not valid json }");

    CategoryServiceImpl service = newService();

    assertThat(service.getAllCategoryNames()).isEmpty();
  }

  @Test
  void saveToFile_overwritesNotAppends() throws IOException {
    CategoryServiceImpl service = newService();
    service.save(USER_ID, "Еда");

    String firstJson = Files.readString(categoriesFile);

    service.save(USER_ID, "Транспорт");

    String secondJson = Files.readString(categoriesFile);
    assertThat(secondJson).contains("ЕДА");
    assertThat(secondJson).contains("ТРАНСПОРТ");
    assertThat(secondJson).isNotEqualTo(firstJson);
  }
}

package ru.osipov.sf.finance.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractEntityService<T> {

  protected final String fileName;
  protected final ObjectMapper mapper;
  private final TypeReference<Map<String, T>> typeRef;
  protected final Map<String, T> storage = new HashMap<>();

  protected AbstractEntityService(
      String fileName, ObjectMapper mapper, TypeReference<Map<String, T>> typeRef) {
    this.fileName = fileName;
    this.mapper = mapper;
    this.typeRef = typeRef;
    loadFromFile();
  }

  protected void loadFromFile() {
    File f = new File(fileName);
    if (!f.exists()) return;
    try {
      Map<String, T> loaded = mapper.readValue(f, typeRef);
      storage.clear();
      storage.putAll(loaded);
    } catch (Exception e) {
      System.out.printf("Не удалось загрузить '%s': %s%n", fileName, e.getMessage());
    }
  }

  protected void saveToFile() {
    try {
      Path tmp = Paths.get(fileName + ".tmp");
      mapper.writerWithDefaultPrettyPrinter().writeValue(tmp.toFile(), storage);
      Files.move(
          tmp,
          Paths.get(fileName),
          StandardCopyOption.REPLACE_EXISTING,
          StandardCopyOption.ATOMIC_MOVE);
    } catch (Exception e) {
      System.out.printf("Не удалось сохранить '%s': %s%n", fileName, e.getMessage());
    }
  }
}

package ru.osipov.sf.finance.services.base;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

/**
 * Базовый класс для сервисов, хранящих данные в JSON-файле в виде {@code Map<String, T>}.
 *
 * <p>При создании загружает данные из файла (если он существует). Предоставляет наследникам методы
 * {@link #loadFromFile()} и {@link #saveToFile()} для чтения и записи хранилища.
 *
 * @param <T> тип хранимой сущности
 */
public abstract class AbstractEntityService<T> {

  protected final String fileName;
  protected final ObjectMapper mapper;
  private final TypeReference<Map<String, T>> typeRef;
  protected final Map<String, T> storage = new HashMap<>();

  /**
   * Создаёт сервис и загружает данные из указанного файла.
   *
   * @param fileName путь к JSON-файлу с данными
   * @param mapper {@link ObjectMapper} для сериализации/десериализации
   * @param typeRef тип хранилища ({@code Map<String, T>}) для Jackson
   */
  protected AbstractEntityService(
      String fileName, ObjectMapper mapper, TypeReference<Map<String, T>> typeRef) {
    this.fileName = fileName;
    this.mapper = mapper;
    this.typeRef = typeRef;
    loadFromFile();
  }

  /**
   * Загружает данные из файла в {@link #storage}. Если файл не существует — хранилище остаётся
   * пустым. При ошибке чтения выводит сообщение в консоль, хранилище не меняется.
   */
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

  /**
   * Сохраняет {@link #storage} в файл. Запись атомарная: сначала во временный {@code .tmp}-файл,
   * затем перемещение с заменой. При ошибке записи выводит сообщение в консоль.
   */
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

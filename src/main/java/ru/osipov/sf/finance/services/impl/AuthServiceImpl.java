package ru.osipov.sf.finance.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import ru.osipov.sf.finance.config.ApplicationConfig;
import ru.osipov.sf.finance.models.entity.User;
import ru.osipov.sf.finance.services.AbstractEntityService;
import ru.osipov.sf.finance.services.AuthService;

public class AuthServiceImpl extends AbstractEntityService<User> implements AuthService {

  public AuthServiceImpl(ApplicationConfig applicationConfig, ObjectMapper mapper) {
    super(applicationConfig.getUserFile(), mapper, new TypeReference<Map<String, User>>() {});
  }

  private String hash(String password) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] digest = md.digest(password.getBytes(StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder();
      for (byte b : digest) sb.append(String.format("%02x", b));
      return sb.toString();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Optional<User> register(String username, String password) {
    if (username == null || username.isBlank() || password == null || password.isBlank()) {
      System.out.println("Логин и пароль не могут быть пустыми.");
      return Optional.empty();
    }
    if (storage.containsKey(username)) {
      System.out.println("Пользователь с таким логином уже существует.");
      return Optional.empty();
    }
    User user = new User(UUID.randomUUID(), username, hash(password));
    storage.put(username, user);
    this.saveToFile();
    System.out.println("Регистрация прошла успешно");
    return Optional.of(user);
  }

  @Override
  public Optional<User> login(String username, String password) {
    User user = storage.get(username);
    if (user == null) {
      System.out.println("Пользователь не найден.");
      return Optional.empty();
    }
    if (!user.getPasswordHash().equals(hash(password))) {
      System.out.println("Неверный пароль.");
      return Optional.empty();
    }
    System.out.println("Вход выполнен. Добро пожаловать, " + username + "!");
    return Optional.of(user);
  }
}

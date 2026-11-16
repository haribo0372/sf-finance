package ru.osipov.sf.finance.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import ru.osipov.sf.finance.config.ApplicationConfig;
import ru.osipov.sf.finance.exception.LoginException;
import ru.osipov.sf.finance.exception.UserAlreadyExists;
import ru.osipov.sf.finance.exception.ValidateInputException;
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
  public User register(String username, String password) {
    if (username == null || username.isBlank() || password == null || password.isBlank()) {
      throw new ValidateInputException("Логин и пароль не могут быть пустыми.");
    }
    if (storage.containsKey(username)) {
      throw new UserAlreadyExists("Пользователь с таким логином уже существует.");
    }
    User user = new User(UUID.randomUUID(), username, hash(password));
    storage.put(username, user);
    this.saveToFile();
    return user;
  }

  @Override
  public User login(String username, String password) {
    User user = storage.get(username);

    if (user == null) throw new LoginException("Пользователь не найден.");

    if (!user.getPasswordHash().equals(hash(password))) {
      throw new LoginException("Неверный пароль.");
    }

    return user;
  }
}

package ru.osipov.sf.finance.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import ru.osipov.sf.finance.config.ApplicationConfig;
import ru.osipov.sf.finance.exception.out.LoginException;
import ru.osipov.sf.finance.exception.out.UserAlreadyExists;
import ru.osipov.sf.finance.exception.out.ValidateInputException;
import ru.osipov.sf.finance.models.entity.User;
import ru.osipov.sf.finance.services.AuthService;
import ru.osipov.sf.finance.services.base.AbstractEntityService;

public class AuthServiceImpl extends AbstractEntityService<User> implements AuthService {

  public AuthServiceImpl(ApplicationConfig applicationConfig, ObjectMapper mapper) {
    super(applicationConfig.getUserFile(), mapper, new TypeReference<Map<String, User>>() {});
  }

  private String hash(String password, UUID uuid) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      md.update(uuid.toString().getBytes(StandardCharsets.UTF_8));
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
    UUID id = UUID.randomUUID();
    User user = new User(id, username, hash(password, id));
    storage.put(username, user);
    this.saveToFile();
    return user;
  }

  @Override
  public User login(String username, String password) {
    User user = storage.get(username);
    if (user == null) throw new LoginException("Пользователь не найден.");
    if (!user.getPasswordHash().equals(hash(password, user.getUuid()))) {
      throw new LoginException("Неверный пароль.");
    }
    return user;
  }
}

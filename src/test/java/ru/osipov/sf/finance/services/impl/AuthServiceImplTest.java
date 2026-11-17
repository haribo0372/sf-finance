package ru.osipov.sf.finance.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.osipov.sf.finance.config.ApplicationConfig;
import ru.osipov.sf.finance.config.JsonConfig;
import ru.osipov.sf.finance.exception.out.LoginException;
import ru.osipov.sf.finance.exception.out.UserAlreadyExists;
import ru.osipov.sf.finance.exception.out.ValidateInputException;
import ru.osipov.sf.finance.models.entity.User;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

  @TempDir Path tempDir;

  @Mock private ApplicationConfig applicationConfig;

  private AuthServiceImpl service;

  @BeforeEach
  void setUp() {
    when(applicationConfig.getUserFile()).thenReturn(tempDir.resolve("users.json").toString());

    JsonConfig jsonConfig = new JsonConfig();
    service = new AuthServiceImpl(applicationConfig, jsonConfig.getMapper());
  }

  @Test
  void register_newUser_success() {
    User user = service.register("alice", "password123");

    assertThat(user).isNotNull();
    assertThat(user.getUuid()).isNotNull();
    assertThat(user.getUsername()).isEqualTo("alice");
    assertThat(user.getPasswordHash()).isNotNull().isNotEqualTo("password123");
  }

  @Test
  void register_hashesPassword() {
    User user = service.register("alice", "password123");

    assertThat(user.getPasswordHash()).hasSize(64);
    assertThat(user.getPasswordHash()).matches("[0-9a-f]{64}");
  }

  @Test
  void register_samePasswordDifferentUsers_differentHashes() {
    User alice = service.register("alice", "password123");
    User bob = service.register("bob", "password123");

    assertThat(alice.getPasswordHash()).isNotEqualTo(bob.getPasswordHash());
  }

  @Test
  void register_duplicateUsername_throws() {
    service.register("alice", "password123");

    assertThatThrownBy(() -> service.register("alice", "other"))
        .isInstanceOf(UserAlreadyExists.class);
  }

  @Test
  void register_emptyUsername_throws() {
    assertThatThrownBy(() -> service.register("", "password123"))
        .isInstanceOf(ValidateInputException.class);
  }

  @Test
  void register_blankUsername_throws() {
    assertThatThrownBy(() -> service.register("   ", "password123"))
        .isInstanceOf(ValidateInputException.class);
  }

  @Test
  void register_emptyPassword_throws() {
    assertThatThrownBy(() -> service.register("alice", ""))
        .isInstanceOf(ValidateInputException.class);
  }

  @Test
  void register_blankPassword_throws() {
    assertThatThrownBy(() -> service.register("alice", "   "))
        .isInstanceOf(ValidateInputException.class);
  }

  @Test
  void login_existingUser_returnsUser() {
    User registered = service.register("alice", "password123");

    User loggedIn = service.login("alice", "password123");

    assertThat(loggedIn).isEqualTo(registered);
  }

  @Test
  void login_wrongPassword_throws() {
    service.register("alice", "password123");

    assertThatThrownBy(() -> service.login("alice", "wrong"))
        .isInstanceOf(LoginException.class)
        .hasMessageContaining("Неверный пароль");
  }

  @Test
  void login_unknownUser_throws() {
    assertThatThrownBy(() -> service.login("unknown", "password123"))
        .isInstanceOf(LoginException.class)
        .hasMessageContaining("Пользователь не найден");
  }

  @Test
  void login_afterRegister_multipleUsers() {
    service.register("alice", "password123");
    service.register("bob", "qwerty");

    assertThat(service.login("alice", "password123").getUsername()).isEqualTo("alice");
    assertThat(service.login("bob", "qwerty").getUsername()).isEqualTo("bob");
  }

  @Test
  void login_caseSensitiveUsername_throws() {
    service.register("alice", "password123");

    assertThatThrownBy(() -> service.login("Alice", "password123"))
        .isInstanceOf(LoginException.class)
        .hasMessageContaining("Пользователь не найден");
  }

  @Test
  void register_differentCaseDifferentUsers_success() {
    User alice = service.register("alice", "password123");
    User aliceUpper = service.register("Alice", "password123");

    assertThat(alice.getUuid()).isNotEqualTo(aliceUpper.getUuid());
    assertThat(service.login("alice", "password123").getUuid()).isEqualTo(alice.getUuid());
    assertThat(service.login("Alice", "password123").getUuid()).isEqualTo(aliceUpper.getUuid());
  }

  @Test
  void login_passwordCaseSensitive_throws() {
    service.register("alice", "password123");

    assertThatThrownBy(() -> service.login("alice", "PASSWORD123"))
        .isInstanceOf(LoginException.class)
        .hasMessageContaining("Неверный пароль");
  }
}

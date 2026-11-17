package ru.osipov.sf.finance.services;

import ru.osipov.sf.finance.models.entity.User;

/** Сервис аутентификации: регистрация и вход пользователей. */
public interface AuthService {

  /**
   * Регистрирует нового пользователя.
   *
   * @param username логин (не пустой, уникальный)
   * @param password пароль в открытом виде (хэшируется внутри)
   * @return созданный пользователь
   * @throws ru.osipov.sf.finance.exception.out.ValidateInputException если логин или пароль пустые
   * @throws ru.osipov.sf.finance.exception.out.UserAlreadyExists если логин уже занят
   */
  User register(String username, String password);

  /**
   * Выполняет вход пользователя.
   *
   * @param username логин
   * @param password пароль в открытом виде
   * @return найденный пользователь
   * @throws ru.osipov.sf.finance.exception.out.LoginException если пользователь не найден или
   *     пароль неверный
   */
  User login(String username, String password);
}

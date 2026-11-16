package ru.osipov.sf.finance.services;

import java.util.Optional;
import ru.osipov.sf.finance.models.entity.User;

public interface AuthService {

  Optional<User> register(String username, String password);

  Optional<User> login(String username, String password);
}

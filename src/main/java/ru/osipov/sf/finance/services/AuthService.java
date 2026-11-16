package ru.osipov.sf.finance.services;

import java.util.Optional;
import ru.osipov.sf.finance.models.entity.User;

public interface AuthService {

  User register(String username, String password);

  User login(String username, String password);
}

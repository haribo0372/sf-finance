package ru.osipov.sf.finance.models.entity;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Модель данных пользователя */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

  private UUID uuid;
  private String username;
  private String passwordHash;
}

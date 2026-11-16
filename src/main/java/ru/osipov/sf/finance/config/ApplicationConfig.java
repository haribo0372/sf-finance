package ru.osipov.sf.finance.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ApplicationConfig {

  private final String userFile;
  private final String transactionFile;
  private final String budgetFile;
  private final String categoryFile;

  public ApplicationConfig() {
    this.userFile = requireEnv("FILE_USER");
    this.transactionFile = requireEnv("FILE_TRANSACTIONS");
    this.budgetFile = requireEnv("FILE_BUDGET");
    this.categoryFile = requireEnv("FILE_CATEGORY");
  }

  private static String requireEnv(String key) {
    String v = System.getenv(key);
    if (v == null || v.isBlank()) {
      throw new IllegalStateException("Env var " + key + " is not set");
    }
    return v.trim();
  }
}

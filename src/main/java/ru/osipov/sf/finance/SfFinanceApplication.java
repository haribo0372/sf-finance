package ru.osipov.sf.finance;

import java.util.Scanner;
import java.util.Set;
import ru.osipov.sf.finance.config.ApplicationConfig;
import ru.osipov.sf.finance.config.JsonConfig;
import ru.osipov.sf.finance.exception.BaseException;
import ru.osipov.sf.finance.exception.CategoryNotFoundException;
import ru.osipov.sf.finance.models.FinancialSummary;
import ru.osipov.sf.finance.models.TransactionData;
import ru.osipov.sf.finance.models.entity.Budget;
import ru.osipov.sf.finance.models.entity.User;
import ru.osipov.sf.finance.models.enums.TransactionType;
import ru.osipov.sf.finance.services.AuthService;
import ru.osipov.sf.finance.services.CategoryService;
import ru.osipov.sf.finance.services.TransactionService;
import ru.osipov.sf.finance.services.impl.AuthServiceImpl;
import ru.osipov.sf.finance.services.impl.CategoryServiceImpl;
import ru.osipov.sf.finance.services.impl.TransactionServiceImpl;
import ru.osipov.sf.finance.utils.ScannerInUtils;

public class SfFinanceApplication {

  private static final Scanner scanner;
  private static final ApplicationConfig applicationConfig;
  private static final JsonConfig jsonConfig;
  private static final AuthService authService;
  private static final TransactionService transactionService;
  private static final CategoryService categoryService;

  private static User currentUser = null;

  static {
    scanner = new Scanner(System.in);

    applicationConfig = new ApplicationConfig();
    jsonConfig = new JsonConfig();

    authService = new AuthServiceImpl(applicationConfig, jsonConfig.getMapper());
    categoryService = new CategoryServiceImpl(applicationConfig, jsonConfig.getMapper());

    transactionService =
        new TransactionServiceImpl(applicationConfig, jsonConfig.getMapper(), categoryService);
  }

  public static void main(String[] args) {
    while (true) {
      if (currentUser == null) {
        guestMenu();
      } else {
        userMenu();
      }
    }
  }

  private static void guestMenu() {
    System.out.println("\n------------------------------------------------------");
    System.out.println("               === ГЛАВНОЕ МЕНЮ ===");
    System.out.println("------------------------------------------------------");
    System.out.println("1. Войти");
    System.out.println("2. Зарегистрироваться");
    System.out.println("0. Выйти из программы");
    System.out.print("Выбор: ");
    String ch = scanner.nextLine().trim();

    switch (ch) {
      case "1" -> doLogin();
      case "2" -> doRegister();
      case "0" -> {
        System.out.println("До свидания!");
        System.exit(0);
      }
      default -> System.out.println("Неверный выбор.");
    }
  }

  private static void userMenu() {
    System.out.println("\n------------------------------------------------------");
    System.out.println("               === МЕНЮ (" + currentUser.getUsername() + ") ===");
    System.out.println("------------------------------------------------------");
    System.out.println("1. Выйти из профиля");
    System.out.println("2. Зарегистрировать транзакцию");
    System.out.println("3. Создать категорию");
    System.out.println("4. Установить бюджет на категорию");
    System.out.println("5. Удалить бюджет на категорию");
    System.out.println("6. Получить общий отчёт транзакций по категории");
    System.out.println("7. Получить краткий отчёт о доходах и расходах");
    System.out.println("8. Получить подробный отчёт о доходах и расходах");
    System.out.println("0. Выйти из программы");
    System.out.print("Выбор: ");
    String ch = scanner.nextLine().trim();

    switch (ch) {
      case "1" -> {
        System.out.println("Вы вышли из профиля.");
        currentUser = null;
      }
      case "2" -> doCreateNewTransaction();
      case "3" -> doCreateNewCategory();
      case "4" -> doCreateNewBudget();
      case "5" -> doRemoveBudget();
      case "6" -> doGetReportByCategory();
      case "7" -> doGetShortReportIncomeExpense();
      case "8" -> doGetReportIncomeExpense();
      case "0" -> {
        System.out.println("До свидания!");
        System.exit(0);
      }
      default -> System.out.println("Неверный выбор.");
    }
  }

  private static void doLogin() {
    String login = ScannerInUtils.readString("Логин: ");
    String password = ScannerInUtils.readString("Пароль: ");
    try {
      currentUser = authService.login(login, password);
      System.out.println("Вход выполнен. Добро пожаловать, " + currentUser.getUsername() + "!");
    } catch (BaseException e) {
      System.out.println(e.getMessage());
    }
  }

  private static void doRegister() {
    String login = ScannerInUtils.readString("Логин: ");
    String password = ScannerInUtils.readString("Пароль: ");

    try {
      currentUser = authService.register(login, password);
      System.out.println("Регистрация прошла успешно");
    } catch (BaseException e) {
      System.out.println(e.getMessage());
    }
  }

  private static void doCreateNewTransaction() {
    TransactionType transactionType = readEnumTransactionType();
    String category = readCategoryName("Категория: ");
    Integer sum =
        ScannerInUtils.readInteger(
            "Сумма: ", integer -> integer > 0, "Сумма должна быть положительная");
    try {
      var registeredTransaction =
          transactionService.registerTransaction(
              currentUser.getUuid(), sum, transactionType, category);
      System.out.println("Транзакция успешно зарегистрирована: " + registeredTransaction);
    } catch (CategoryNotFoundException e) {
      System.out.println(e.getMessage());
      printAllCategories();
    } catch (BaseException e) {
      System.out.println(e.getMessage());
    }
  }

  private static void doCreateNewCategory() {
    String category = readCategoryName("Имя новой категории: ");
    try {
      var savedCategory = categoryService.save(currentUser.getUuid(), category);
      System.out.println("Категория успешно создана: " + savedCategory);
    } catch (BaseException e) {
      System.out.println(e.getMessage());
    }
  }

  private static void doCreateNewBudget() {
    String category = readCategoryName("Категория: ");
    Integer sum =
        ScannerInUtils.readInteger(
            "Сумма: ", integer -> integer > 0, "Сумма должна быть положительная");

    try {
      Budget registeredBudget =
          transactionService.registerBudget(currentUser.getUuid(), sum, category);
      System.out.println("Бюджет успешно зарегистрирован: " + registeredBudget);
    } catch (CategoryNotFoundException e) {
      System.out.println(e.getMessage());
      printAllCategories();
    } catch (BaseException e) {
      System.out.println(e.getMessage());
    }
  }

  private static void doRemoveBudget() {
    String category = readCategoryName("Категория: ");
    try {
      transactionService.removeBudget(currentUser.getUuid(), category);
      System.out.println("Бюджет успешно удален");
    } catch (BaseException e) {
      System.out.println(e.getMessage());
    }
  }

  private static void doGetReportByCategory() {
    String category = readCategoryName("Категория: ");
    try {
      TransactionData txData =
          transactionService.findTransactionsByUserIdAndCategory(currentUser.getUuid(), category);

      Budget budget = txData.getBudget();

      System.out.println();
      System.out.println("=== Отчёт по категории ===");
      System.out.printf("Категория:       %s%n", txData.getCategory());
      System.out.printf(
          "Бюджет:          %s%n", budget == null ? "<не установлен>" : budget.getSum());
      System.out.println("--------------------------");
      System.out.printf("Сумма доходов:   %s%n", txData.getIncomeSum());
      System.out.printf("Сумма расходов:  %s%n", txData.getExpenseSum());
      System.out.println("--------------------------");

      if (txData.getTransactions().isEmpty()) {
        System.out.println("    (нет)");
        System.out.println("==========================");
        return;
      }

      for (var tx : txData.getTransactions()) {
        System.out.printf(
            "    - Дата: %s | Тип: %-7s | Сумма: %s%n",
            tx.getDate(), tx.getType().getLabel(), tx.getSum());
      }

      System.out.println("==========================");
    } catch (BaseException e) {
      System.out.println(e.getMessage());
    }
  }

  private static void doGetShortReportIncomeExpense() {
    try {
      FinancialSummary summary =
          transactionService.findFinancialSummaryByUserId(currentUser.getUuid());
      System.out.println();
      System.out.println("=== Краткий отчёт ===");
      System.out.printf("Сумма доходов:   %s%n", summary.getIncomeSum());
      System.out.printf("Сумма расходов:  %s%n", summary.getExpenseSum());
      System.out.println("=====================");
    } catch (BaseException e) {
      System.out.println(e.getMessage());
    }
  }

  private static void doGetReportIncomeExpense() {
    try {
      FinancialSummary summary =
          transactionService.findFinancialSummaryByUserId(currentUser.getUuid());
      System.out.println();
      System.out.println("=== Подробный отчёт ===");
      System.out.printf("Сумма всех доходов:   %s%n", summary.getIncomeSum());
      System.out.printf("Сумма всех расходов:  %s%n", summary.getExpenseSum());
      System.out.println("=======================");

      if (summary.getTransactionsByCategory().isEmpty()) {
        System.out.println("Транзакций нет.");
        return;
      }

      for (var entry : summary.getTransactionsByCategory().entrySet()) {
        String category = entry.getKey();
        TransactionData txData = entry.getValue();
        Budget budget = txData.getBudget();

        System.out.println();
        System.out.printf("Категория: %s%n", category);
        System.out.printf("  Бюджет:  %s%n", budget == null ? "<не установлен>" : budget.getSum());
        System.out.printf("  Доходы:  %s%n", txData.getIncomeSum());
        System.out.printf("  Расходы: %s%n", txData.getExpenseSum());
        System.out.println("  Транзакции:");

        if (txData.getTransactions().isEmpty()) {
          System.out.println("    (нет)");
          continue;
        }

        for (var tx : txData.getTransactions()) {
          System.out.printf(
              "    - Дата: %s | Тип: %-7s | Сумма: %s%n",
              tx.getDate(), tx.getType().getLabel(), tx.getSum());
        }
      }
      System.out.println("=======================");
    } catch (BaseException e) {
      System.out.println(e.getMessage());
    }
  }

  private static TransactionType readEnumTransactionType() {
    return ScannerInUtils.readEnum(TransactionType.class, "Доступные типы транзакции:");
  }

  private static String readCategoryName(String title) {
    printAllCategories();
    return ScannerInUtils.readString(
        title,
        s -> !s.isBlank() && s.length() <= 16,
        "строка не может быть пустой, макимальная длина строки 16");
  }

  private static void printAllCategories() {
    System.out.println("Доступные категории:");
    Set<String> categories = categoryService.getAllCategoryNames();
    if (categories.isEmpty()) {
      System.out.println("\t<Категорий не найдено>");
    } else {
      int n = 0;
      for (var categoryName : categories) {
        System.out.printf("\t%s. %s%n", ++n, categoryName);
      }
    }
  }
}

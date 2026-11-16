package ru.osipov.sf.finance;

import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import ru.osipov.sf.finance.config.ApplicationConfig;
import ru.osipov.sf.finance.config.JsonConfig;
import ru.osipov.sf.finance.exception.BaseException;
import ru.osipov.sf.finance.exception.CategoryNotFoundException;
import ru.osipov.sf.finance.models.entity.User;
import ru.osipov.sf.finance.models.enums.TransactionType;
import ru.osipov.sf.finance.services.AuthService;
import ru.osipov.sf.finance.services.BudgetService;
import ru.osipov.sf.finance.services.CategoryService;
import ru.osipov.sf.finance.services.TransactionService;
import ru.osipov.sf.finance.services.impl.AuthServiceImpl;
import ru.osipov.sf.finance.services.impl.BudgetServiceImpl;
import ru.osipov.sf.finance.services.impl.CategoryServiceImpl;
import ru.osipov.sf.finance.services.impl.TransactionServiceImpl;
import ru.osipov.sf.finance.utils.ScannerInUtils;

public class SfFinanceApplication {

  private static final Scanner scanner;
  private static final ApplicationConfig applicationConfig;
  private static final JsonConfig jsonConfig;
  private static final AuthService authService;
  private static final TransactionService transactionService;
  private static final BudgetService budgetService;
  private static final CategoryService categoryService;

  private static User currentUser = null;

  static {
    scanner = new Scanner(System.in);

    applicationConfig = new ApplicationConfig();
    jsonConfig = new JsonConfig();

    authService = new AuthServiceImpl(applicationConfig, jsonConfig.getMapper());
    categoryService = new CategoryServiceImpl(applicationConfig, jsonConfig.getMapper());

    budgetService =
        new BudgetServiceImpl(applicationConfig, jsonConfig.getMapper(), categoryService);
    transactionService =
        new TransactionServiceImpl(
            applicationConfig, jsonConfig.getMapper(), budgetService, categoryService);
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
    System.out.println("\t=== ГЛАВНОЕ МЕНЮ ===");
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
    System.out.println("\t=== МЕНЮ (" + currentUser.getUsername() + ") ===");
    System.out.println("------------------------------------------------------");
    System.out.println("1. Выйти из профиля");
    System.out.println("2. Зарегистрировать транзакцию");
    System.out.println("3. Создать категорию");
    System.out.println("4. Установить/обновить бюджет на категорию");
    System.out.println("5. ");
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
      case "5" -> {}
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

    Optional<User> user = authService.login(login, password);
    user.ifPresent(u -> currentUser = u);
  }

  private static void doRegister() {
    String login = ScannerInUtils.readString("Логин: ");
    String password = ScannerInUtils.readString("Пароль: ");

    Optional<User> user = authService.register(login, password);
    user.ifPresent(u -> currentUser = u);
  }

  private static void doCreateNewTransaction() {
    TransactionType transactionType = readEnumTransactionType();
    String category = readCategoryName("Категория: ");
    Integer sum =
        ScannerInUtils.readInteger(
            "Сумма: ", integer -> integer > 0, "Сумма должна быть положительная");
    try {
      transactionService.save(currentUser.getUuid(), sum, transactionType, category);
    } catch (CategoryNotFoundException e) {
      System.out.println(e.getMessage());
      printAllCategories();
    } catch (BaseException e) {
      System.out.println(e.getMessage());
    }
  }

  private static void doCreateNewCategory() {
    printAllCategories();
    String category = readCategoryName("Имя новой категории: ");
    categoryService.save(currentUser.getUuid(), category);
  }

  private static void doCreateNewBudget() {
    printAllCategories();
    String category = readCategoryName("Категория: ");
    Integer sum =
        ScannerInUtils.readInteger(
            "Сумма: ", integer -> integer > 0, "Сумма должна быть положительная");
    try {
      budgetService.save(currentUser.getUuid(), sum, category);
    } catch (CategoryNotFoundException e) {
      System.out.println(e.getMessage());
      printAllCategories();
    } catch (BaseException e) {
      System.out.println(e.getMessage());
    }
  }

  private static TransactionType readEnumTransactionType() {
    return ScannerInUtils.readEnum(TransactionType.class, "Доступные типы транзакции:");
  }

  private static String readCategoryName(String title) {
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

package ru.osipov.sf.finance;

import java.util.Scanner;
import java.util.Set;
import ru.osipov.sf.finance.config.ApplicationConfig;
import ru.osipov.sf.finance.config.JsonConfig;
import ru.osipov.sf.finance.exception.base.BaseException;
import ru.osipov.sf.finance.exception.out.CategoryNotFoundException;
import ru.osipov.sf.finance.exception.out.EofException;
import ru.osipov.sf.finance.models.aggregate.FinancialSummary;
import ru.osipov.sf.finance.models.aggregate.TransactionData;
import ru.osipov.sf.finance.models.entity.Budget;
import ru.osipov.sf.finance.models.entity.User;
import ru.osipov.sf.finance.models.enums.TransactionType;
import ru.osipov.sf.finance.models.result.TransactionResult;
import ru.osipov.sf.finance.modules.printers.ReportPrinter;
import ru.osipov.sf.finance.modules.printers.WarningPrinter;
import ru.osipov.sf.finance.modules.readers.ConsoleInputReader;
import ru.osipov.sf.finance.services.AuthService;
import ru.osipov.sf.finance.services.CategoryService;
import ru.osipov.sf.finance.services.TransactionService;
import ru.osipov.sf.finance.services.impl.AuthServiceImpl;
import ru.osipov.sf.finance.services.impl.CategoryServiceImpl;
import ru.osipov.sf.finance.services.impl.TransactionServiceImpl;

public class SfFinanceApplication {

  private static final int MAX_CATEGORY_NAME_LENGTH = 16;

  private static final Scanner scanner;
  private static final ConsoleInputReader consoleInputReader;
  private static final ApplicationConfig applicationConfig;
  private static final JsonConfig jsonConfig;
  private static final AuthService authService;
  private static final TransactionService transactionService;
  private static final CategoryService categoryService;

  private static User currentUser = null;

  static {
    scanner = new Scanner(System.in);
    consoleInputReader = new ConsoleInputReader(scanner);

    applicationConfig = new ApplicationConfig();
    jsonConfig = new JsonConfig();

    authService = new AuthServiceImpl(applicationConfig, jsonConfig.getMapper());
    categoryService = new CategoryServiceImpl(applicationConfig, jsonConfig.getMapper());
    transactionService =
        new TransactionServiceImpl(applicationConfig, jsonConfig.getMapper(), categoryService);
  }

  public static void main(String[] args) {
    while (true) {
      try {
        if (currentUser == null) guestMenu();
        else userMenu();
      } catch (EofException e) {
        System.out.println(e.getMessage());
      } catch (Exception e) {
        System.out.println("Непредвиденная ошибка: " + e.getMessage());
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
    String login = consoleInputReader.readString("Логин: ");
    String password = consoleInputReader.readString("Пароль: ");
    try {
      currentUser = authService.login(login, password);
      System.out.println("Вход выполнен. Добро пожаловать, " + currentUser.getUsername() + "!");
    } catch (BaseException e) {
      System.out.println(e.getMessage());
    }
  }

  private static void doRegister() {
    String login = consoleInputReader.readString("Логин: ");
    String password = consoleInputReader.readString("Пароль: ");

    try {
      currentUser = authService.register(login, password);
      System.out.println("Регистрация прошла успешно");
    } catch (BaseException e) {
      System.out.println(e.getMessage());
    }
  }

  private static void doCreateNewTransaction() {
    String category = readCategoryName("Категория (название): ");
    TransactionType transactionType = readEnumTransactionType();
    Integer sum =
        consoleInputReader.readInteger(
            "Сумма: ", integer -> integer > 0, "Сумма должна быть положительная");
    try {
      TransactionResult result =
          transactionService.registerTransaction(
              currentUser.getUuid(), sum, transactionType, category);
      System.out.println("Транзакция успешно зарегистрирована: " + result.getTransaction());
      WarningPrinter.printWarnings(result.getWarnings());
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
    String category = readCategoryName("Категория (название): ");
    Integer sum =
        consoleInputReader.readInteger(
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
    String category = readCategoryName("Категория (название): ");
    try {
      transactionService.removeBudget(currentUser.getUuid(), category);
      System.out.println("Бюджет успешно удален");
    } catch (BaseException e) {
      System.out.println(e.getMessage());
    }
  }

  private static void doGetReportByCategory() {
    String category = readCategoryName("Категория (название): ");
    try {
      TransactionData txData =
          transactionService.findTransactionsByUserIdAndCategory(currentUser.getUuid(), category);
      ReportPrinter.printReportByCategory(txData);
    } catch (BaseException e) {
      System.out.println(e.getMessage());
    }
  }

  private static void doGetShortReportIncomeExpense() {
    try {
      FinancialSummary summary =
          transactionService.findFinancialSummaryByUserId(currentUser.getUuid());
      ReportPrinter.printShortReport(summary);
    } catch (BaseException e) {
      System.out.println(e.getMessage());
    }
  }

  private static void doGetReportIncomeExpense() {
    try {
      FinancialSummary summary =
          transactionService.findFinancialSummaryByUserId(currentUser.getUuid());
      ReportPrinter.printFullReport(summary);
    } catch (BaseException e) {
      System.out.println(e.getMessage());
    }
  }

  private static TransactionType readEnumTransactionType() {
    return consoleInputReader.readEnum(TransactionType.class, "Доступные типы транзакции:");
  }

  private static String readCategoryName(String title) {
    printAllCategories();
    return consoleInputReader.readString(
        title, s -> s.length() <= 16, "максимальная длина строки " + MAX_CATEGORY_NAME_LENGTH);
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

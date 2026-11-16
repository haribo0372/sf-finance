package ru.osipov.sf.finance.utils;

import java.util.Scanner;
import java.util.function.Function;
import java.util.function.Predicate;
import ru.osipov.sf.finance.exception.ValidateInputException;
import ru.osipov.sf.finance.models.enums.Labeled;

public final class ScannerInUtils {
  private static final String TEMPLATE_ERROR = "Ошибка ввода: %s.%n";
  private static Scanner scanner = new Scanner(System.in);

  private ScannerInUtils() {}

  public static <T> T read(String title, Function<String, T> parser) {
    while (true) {
      System.out.print(title);
      String line = scanner.nextLine().trim();
      try {
        return parser.apply(line);
      } catch (ValidateInputException e) {
        System.out.printf(TEMPLATE_ERROR, e.getMessage());
      }
    }
  }

  public static String readString(String title, Predicate<String> predicate, String errorMessage) {
    return read(
        title,
        s -> {
          if (predicate.test(s)) return s;
          throw new ValidateInputException(errorMessage);
        });
  }

  public static String readString(String title) {
    return readString(title, s -> !s.isBlank(), "строка не может быть пустой");
  }

  public static int readInteger(String title, Predicate<Integer> predicate, String errorMessage) {
    return read(
        title,
        s -> {
          try {
            Integer integer = Integer.parseInt(s);
            if (predicate.test(integer)) return integer;
            throw new ValidateInputException(errorMessage);
          } catch (NumberFormatException e) {
            throw new ValidateInputException("ожидается целое число");
          }
        });
  }

  public static <T extends Enum<T> & Labeled> T readEnum(Class<T> enumClass, String title) {
    T[] all = enumClass.getEnumConstants();

    System.out.println(title);
    for (int i = 0; i < all.length; i++) {
      System.out.printf("\t%d. %s%n", i + 1, all[i].getLabel());
    }

    return read(
        "Номер: ",
        s -> {
          int n;
          try {
            n = Integer.parseInt(s);
          } catch (NumberFormatException e) {
            throw new ValidateInputException("ожидается целое число");
          }
          if (n < 1 || n > all.length) {
            throw new ValidateInputException("нет такого номера, диапазон 1.." + all.length);
          }
          return all[n - 1];
        });
  }
}

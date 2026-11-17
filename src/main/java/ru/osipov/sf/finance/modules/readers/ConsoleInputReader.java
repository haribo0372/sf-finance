package ru.osipov.sf.finance.modules.readers;

import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import ru.osipov.sf.finance.exception.out.EofException;
import ru.osipov.sf.finance.exception.out.ValidateInputException;
import ru.osipov.sf.finance.models.enums.Labeled;

@RequiredArgsConstructor
public final class ConsoleInputReader {

  private final Scanner scanner;

  public <T> T read(String title, Function<String, T> parser) {
    while (true) {
      System.out.print(title);
      try {
        String line = scanner.nextLine().trim();
        return parser.apply(line);
      } catch (NoSuchElementException e) {
        throw new EofException();
      } catch (ValidateInputException e) {
        System.out.println("Ошибка ввода: " + e.getMessage() + ".");
      }
    }
  }

  public String readString(String title, Predicate<String> predicate, String errorMessage) {
    return read(
        title,
        s -> {
          if (s.isBlank()) {
            throw new ValidateInputException("строка не может быть пустой");
          }
          if (!predicate.test(s)) {
            throw new ValidateInputException(errorMessage);
          }
          return s;
        });
  }

  public String readString(String title) {
    return read(
        title,
        s -> {
          if (s.isBlank()) {
            throw new ValidateInputException("строка не может быть пустой");
          }
          return s;
        });
  }

  public int readInteger(String title, Predicate<Integer> predicate, String errorMessage) {
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

  public <T extends Enum<T> & Labeled> T readEnum(Class<T> enumClass, String title) {
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

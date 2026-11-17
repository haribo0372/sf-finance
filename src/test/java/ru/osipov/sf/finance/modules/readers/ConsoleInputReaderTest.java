package ru.osipov.sf.finance.modules.readers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.osipov.sf.finance.exception.out.EofException;
import ru.osipov.sf.finance.exception.out.ValidateInputException;
import ru.osipov.sf.finance.models.enums.TransactionType;

class ConsoleInputReaderTest {

  private ByteArrayOutputStream outContent;
  private PrintStream originalOut;

  @BeforeEach
  void setUp() {
    originalOut = System.out;
    outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));
  }

  @AfterEach
  void tearDown() {
    System.setOut(originalOut);
  }

  private ConsoleInputReader withInput(String input) {
    return new ConsoleInputReader(new Scanner(new ByteArrayInputStream(input.getBytes())));
  }

  @Test
  void readString_validInput_returnsValue() {
    ConsoleInputReader utils = withInput("hello\n");

    String result = utils.readString("Ввод: ");

    assertThat(result).isEqualTo("hello");
  }

  @Test
  void readString_blankThenValid_retriesAndReturns() {
    ConsoleInputReader utils = withInput("\nhello\n");

    String result = utils.readString("Ввод: ");

    assertThat(result).isEqualTo("hello");
    assertThat(outContent.toString()).contains("строка не может быть пустой");
  }

  @Test
  void readString_withPredicate_validInput_returnsValue() {
    ConsoleInputReader utils = withInput("short\n");

    String result = utils.readString("Ввод: ", s -> s.length() <= 10, "максимум 10 символов");

    assertThat(result).isEqualTo("short");
  }

  @Test
  void readString_withPredicate_invalidThenValid_retriesAndReturns() {
    ConsoleInputReader utils = withInput("toolongvalue1234567890\nshort\n");

    String result = utils.readString("Ввод: ", s -> s.length() <= 10, "максимум 10 символов");

    assertThat(result).isEqualTo("short");
    assertThat(outContent.toString()).contains("максимум 10 символов");
  }

  @Test
  void readInteger_validInput_returnsValue() {
    ConsoleInputReader utils = withInput("42\n");

    int result = utils.readInteger("Число: ", i -> i > 0, "должно быть > 0");

    assertThat(result).isEqualTo(42);
  }

  @Test
  void readInteger_notNumberThenValid_retriesAndReturns() {
    ConsoleInputReader utils = withInput("abc\n42\n");

    int result = utils.readInteger("Число: ", i -> i > 0, "должно быть > 0");

    assertThat(result).isEqualTo(42);
    assertThat(outContent.toString()).contains("ожидается целое число");
  }

  @Test
  void readInteger_negativeThenValid_retriesAndReturns() {
    ConsoleInputReader utils = withInput("-5\n10\n");

    int result = utils.readInteger("Число: ", i -> i > 0, "должно быть > 0");

    assertThat(result).isEqualTo(10);
    assertThat(outContent.toString()).contains("должно быть > 0");
  }

  @Test
  void readEnum_validInput_returnsEnum() {
    ConsoleInputReader utils = withInput("1\n");

    TransactionType result = utils.readEnum(TransactionType.class, "Тип:");

    assertThat(result).isEqualTo(TransactionType.values()[0]);
  }

  @Test
  void readEnum_invalidNumberThenValid_retriesAndReturns() {
    ConsoleInputReader utils = withInput("99\n1\n");

    TransactionType result = utils.readEnum(TransactionType.class, "Тип:");

    assertThat(result).isEqualTo(TransactionType.values()[0]);
    assertThat(outContent.toString()).contains("нет такого номера");
  }

  @Test
  void readEnum_notNumberThenValid_retriesAndReturns() {
    ConsoleInputReader utils = withInput("abc\n2\n");

    TransactionType result = utils.readEnum(TransactionType.class, "Тип:");

    assertThat(result).isEqualTo(TransactionType.values()[1]);
    assertThat(outContent.toString()).contains("ожидается целое число");
  }

  @Test
  void read_customParser_validInput_returnsValue() {
    ConsoleInputReader utils = withInput("123\n");

    Integer result = utils.read("Число: ", Integer::parseInt);

    assertThat(result).isEqualTo(123);
  }

  @Test
  void read_customParser_invalidThenValid_retriesAndReturns() {
    ConsoleInputReader utils = withInput("abc\n123\n");

    Integer result =
        utils.read(
            "Число: ",
            s -> {
              try {
                return Integer.parseInt(s);
              } catch (NumberFormatException e) {
                throw new ValidateInputException("не число");
              }
            });

    assertThat(result).isEqualTo(123);
    assertThat(outContent.toString()).contains("не число");
  }

  @Test
  void read_emptyInput_throwsEofException() {
    ConsoleInputReader utils = withInput("");

    assertThatThrownBy(() -> utils.readString("Ввод: ")).isInstanceOf(EofException.class);
  }
}

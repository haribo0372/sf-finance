package ru.osipov.sf.finance.services;

import java.util.UUID;
import ru.osipov.sf.finance.models.aggregate.FinancialSummary;
import ru.osipov.sf.finance.models.aggregate.TransactionData;
import ru.osipov.sf.finance.models.entity.Budget;
import ru.osipov.sf.finance.models.enums.TransactionType;
import ru.osipov.sf.finance.models.result.TransactionResult;

/**
 * Сервис транзакций и бюджетов: регистрация операций, управление бюджетами по категориям и
 * получение сводок.
 */
public interface TransactionService {

  /**
   * Регистрирует транзакцию (доход или расход) в категории. Для расходов дополнительно проверяет
   * превышение доходов и бюджета, возвращая соответствующие уведомления.
   *
   * @param userId идентификатор пользователя
   * @param sum сумма транзакции (положительная)
   * @param type тип транзакции (доход/расход)
   * @param categoryName имя категории
   * @return результат с зарегистрированной транзакцией и списком уведомлений (пустым, если
   *     превышений нет)
   * @throws ru.osipov.sf.finance.exception.out.CategoryNotFoundException если категория не найдена
   */
  TransactionResult registerTransaction(
      UUID userId, Integer sum, TransactionType type, String categoryName);

  /**
   * Устанавливает бюджет на категорию.
   *
   * @param userId идентификатор пользователя
   * @param sum сумма бюджета (положительная)
   * @param categoryName имя категории
   * @return созданный бюджет
   * @throws ru.osipov.sf.finance.exception.out.CategoryNotFoundException если категория не найдена
   * @throws ru.osipov.sf.finance.exception.out.BudgetAlreadyExistsException если бюджет на
   *     категорию уже установлен
   */
  Budget registerBudget(UUID userId, Integer sum, String categoryName);

  /**
   * Удаляет бюджет с категории.
   *
   * @param userId идентификатор пользователя
   * @param categoryName имя категории
   * @throws ru.osipov.sf.finance.exception.out.CategoryNotFoundException если категория не найдена
   * @throws ru.osipov.sf.finance.exception.out.BudgetNotFoundException если бюджет не установлен
   */
  void removeBudget(UUID userId, String categoryName);

  /**
   * Возвращает данные по категории: транзакции, суммы доходов/расходов и бюджет.
   *
   * @param userId идентификатор пользователя
   * @param categoryName имя категории
   * @return данные по категории; если транзакций нет — пустой {@link TransactionData}
   * @throws ru.osipov.sf.finance.exception.out.CategoryNotFoundException если категория не найдена
   * @throws ru.osipov.sf.finance.exception.out.FinancialSummaryNotFoundException если у
   *     пользователя нет сводки
   */
  TransactionData findTransactionsByUserIdAndCategory(UUID userId, String categoryName);

  /**
   * Возвращает финансовую сводку пользователя (все категории, транзакции, бюджеты).
   *
   * @param userId идентификатор пользователя
   * @return финансовая сводка
   * @throws ru.osipov.sf.finance.exception.out.FinancialSummaryNotFoundException если у
   *     пользователя нет сводки
   */
  FinancialSummary findFinancialSummaryByUserId(UUID userId);
}

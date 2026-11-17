package ru.osipov.sf.finance;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SfFinanceApplicationTest {

  @Test
  void classLoads() {
    assertThat(SfFinanceApplication.class).isNotNull();
  }
}

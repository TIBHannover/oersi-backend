package org.oersi.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BaseEntityTest {

  @Test
  void testEquals() {
    Metadata dummy = new Metadata();
    Metadata dummy2 = new Metadata();
    assertThat(dummy.equals(dummy)).isTrue();
    assertThat(dummy.equals(dummy2)).isFalse();
    dummy.setId(1L);
    dummy2.setId(1L);
    assertThat(dummy.equals(dummy2)).isTrue();
    dummy2.setId(2L);
    assertThat(dummy.equals(dummy2)).isFalse();
  }
}

package org.oersi.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.oersi.domain.LabelDefinition;
import org.oersi.repository.LabelDefinitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
class LabelDefinitionServiceTest {

  @Autowired
  private LabelDefinitionService service;
  @MockBean
  private LabelDefinitionRepository repository;
  @MockBean
  private JavaMailSender mailSender;

  @AfterEach
  void tearDown() {
    service.clearCache();
  }

  private LabelDefinition getTestData() {
    LabelDefinition definition = new LabelDefinition();
    definition.setIdentifier("XXX");
    return definition;
  }

  @Test
  void testCreateOrUpdateWithoutExistingData() {
    List<LabelDefinition> data = List.of(getTestData());
    when(repository.findAll()).thenReturn(List.of());
    service.createOrUpdate(data);
    verify(repository, times(1)).saveAll(data);
  }

  @Test
  void testCreateOrUpdateWithExistingData() {
    List<LabelDefinition> existing = List.of(getTestData());
    when(repository.findAll()).thenReturn(existing);
    List<LabelDefinition> data = new ArrayList<>();
    data.add(getTestData());
    data.get(0).setIdentifier("ABC");
    data.addAll(existing);
    service.createOrUpdate(data);
    verify(repository, times(1)).saveAll(data);
  }


  @Test
  void testDelete() {
    LabelDefinition data = getTestData();
    service.delete(data);
    verify(repository, times(1)).delete(data);
  }

  @Test
  void testFindById() {
    LabelDefinition data = getTestData();
    when(repository.findById(1L)).thenReturn(Optional.of(data));
    LabelDefinition result = service.findById(1L);
    assertThat(result).isNotNull();
  }

  @Test
  void testFindByIllegalId() {
    LabelDefinition result = service.findById(null);
    assertThat(result).isNull();
  }
}

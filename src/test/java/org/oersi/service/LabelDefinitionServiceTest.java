package org.oersi.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oersi.domain.LabelDefinition;
import org.oersi.domain.LocalizedString;
import org.oersi.repository.LabelDefinitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class LabelDefinitionServiceTest {

  @Autowired
  private LabelDefinitionService service;
  @MockBean
  private LabelDefinitionRepository repository;
  @MockBean
  private JavaMailSender mailSender;

  @BeforeEach
  void cleanup() {
    ((LabelDefinitionServiceImpl)service).clearCache();
  }

  @AfterEach
  void tearDown() {
    ((LabelDefinitionServiceImpl)service).clearCache();
  }

  private LabelDefinition getTestData() {
    LabelDefinition definition = new LabelDefinition();
    definition.setIdentifier("XXX");
    LocalizedString localizedString = new LocalizedString();
    Map<String, String> localizedStrings = new HashMap<>();
    localizedStrings.put("de", "test1");
    localizedStrings.put("en", "test2");
    localizedString.setLocalizedStrings(localizedStrings);
    definition.setLabel(localizedString);
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

  @Test
  void testFindLocalizedLabelWithIllegalId() {
    Map<String, String> result = service.findLocalizedLabelByIdentifier(null);
    assertThat(result).isNull();
  }

  @Test
  void testFindLocalizedLabelWithUndefinedLocalizedString() {
    List<LabelDefinition> existing = List.of(getTestData());
    existing.get(0).setLabel(new LocalizedString());
    when(repository.findAll()).thenReturn(existing);
    Map<String, String> result = service.findLocalizedLabelByIdentifier("XXX");
    assertThat(result).isEmpty();

    existing.get(0).setLabel(null);
    when(repository.findAll()).thenReturn(existing);
    result = service.findLocalizedLabelByIdentifier("XXX");
    assertThat(result).isEmpty();
  }

  @Test
  void testFindLocalizedLabel() {
    List<LabelDefinition> existing = List.of(getTestData());
    when(repository.findAll()).thenReturn(existing);
    Map<String, String> result = service.findLocalizedLabelByIdentifier("XXX");
    assertThat(result).hasSize(2).containsKeys("de", "en");
    result = service.findLocalizedLabelByIdentifier("XXX"); // second time for cached result
    assertThat(result).hasSize(2).containsKeys("de", "en");
  }

  @Test
  void testFindLocalizedLabelAndUpdate() {
    List<LabelDefinition> existing = List.of(getTestData());
    when(repository.findAll()).thenReturn(existing);
    Map<String, String> result = service.findLocalizedLabelByIdentifier("XXX");
    assertThat(result).hasSize(2).containsKeys("de", "en").containsEntry("de", "test1");

    List<LabelDefinition> newData = List.of(getTestData());
    newData.get(0).getLabel().setLocalizedStrings(Map.of("de", "test3"));
    service.createOrUpdate(newData);
    when(repository.findAll()).thenReturn(newData);
    result = service.findLocalizedLabelByIdentifier("XXX"); // second time for cached result
    assertThat(result).hasSize(1).containsEntry("de", "test3");
  }

}

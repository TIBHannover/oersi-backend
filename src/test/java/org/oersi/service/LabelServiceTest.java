package org.oersi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oersi.domain.Label;
import org.oersi.repository.LabelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
class LabelServiceTest {

  @Autowired
  private LabelService service;
  @MockBean
  private LabelRepository repository;

  @BeforeEach
  void cleanup() {
    service.clearCache();
  }

  private Label newLabel() {
    Label label = new Label();
    label.setLabelKey("key");
    label.setGroupId("group");
    label.setLabelValue("value");
    label.setLanguageCode("en");
    return label;
  }

  @Test
  void testCreateOrUpdateWithoutExistingData() {
    Label label = newLabel();
    service.createOrUpdate(label.getLanguageCode(), label.getLabelKey(), label.getLabelValue(), label.getGroupId());
    verify(repository, times(1)).save(label);
  }


  @Test
  void testCreateOrUpdateWithExistingDataFoundByIdWithoutChange() {
    Label label = newLabel();
    when(repository.findByLanguageCodeAndLabelKey(label.getLanguageCode(), label.getLabelKey())).thenReturn(Optional.of(label));
    service.createOrUpdate(label.getLanguageCode(), label.getLabelKey(), label.getLabelValue(), label.getGroupId());
    verify(repository, times(0)).save(label);
  }

  @Test
  void testCreateOrUpdateWithExistingDataFoundByIdWithChangedValue() {
    Label label = newLabel();
    when(repository.findByLanguageCodeAndLabelKey(label.getLanguageCode(), label.getLabelKey())).thenReturn(Optional.of(label));
    service.createOrUpdate(label.getLanguageCode(), label.getLabelKey(), "changedValue", label.getGroupId());
    verify(repository, times(1)).save(label);
  }

  @Test
  void testCreateOrUpdateWithExistingDataFoundByIdWithChangedGroup() {
    Label label = newLabel();
    when(repository.findByLanguageCodeAndLabelKey(label.getLanguageCode(), label.getLabelKey())).thenReturn(Optional.of(label));
    service.createOrUpdate(label.getLanguageCode(), label.getLabelKey(), label.getLabelValue(), "changedGroup");
    verify(repository, times(1)).save(label);
  }

  @Test
  void testFindByLanguage() {
    Label label = newLabel();
    when(repository.findAll()).thenReturn(List.of(label));
    Map<String, String> result = service.findByLanguage(label.getLanguageCode());
    assertThat(result).isNotNull()
      .containsEntry(label.getLabelKey(), label.getLabelValue());
    result = service.findByLanguage(label.getLanguageCode());
    assertThat(result).isNotNull()
      .containsEntry(label.getLabelKey(), label.getLabelValue());
  }

  @Test
  void testFindByLanguageAndGroup() {
    Label label = newLabel();
    when(repository.findAll()).thenReturn(List.of(label));
    Map<String, String> result = service.findByLanguageAndGroup(label.getLanguageCode(), label.getGroupId());
    assertThat(result).isNotNull()
      .containsEntry(label.getLabelKey(), label.getLabelValue());
    result = service.findByLanguageAndGroup(label.getLanguageCode(), label.getGroupId());
    assertThat(result).isNotNull()
      .containsEntry(label.getLabelKey(), label.getLabelValue());
  }

}

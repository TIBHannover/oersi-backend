package org.oersi.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.oersi.ElasticsearchServicesMock;
import org.oersi.domain.Label;
import org.oersi.repository.LabelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@Import(ElasticsearchServicesMock.class)
class LabelServiceTest {

  @Autowired
  private LabelService service;
  @Autowired
  private LabelRepository repository; // mock from ElasticsearchServicesMock
  @MockBean
  private JavaMailSender mailSender;

  @BeforeEach
  void cleanup() {
    service.clearCache();
  }

  @AfterEach
  void tearDown() {
    service.clearCache();
  }

  private Label newLabel() {
    Label label = new Label();
    label.setLabelKey("key");
    label.setGroupId("subject");
    label.setField("about");
    label.setLabelValue("value");
    label.setLanguageCode("en");
    return label;
  }

  @Test
  void testCreateOrUpdateWithoutExistingData() {
    Label label = newLabel();
    when(repository.findAll()).thenReturn(List.of());
    when(repository.findByLanguageCodeAndLabelKey(label.getLanguageCode(), label.getLabelKey())).thenReturn(Optional.empty());
    service.createOrUpdate(label.getLanguageCode(), label.getLabelKey(), label.getLabelValue(), label.getField());
    ArgumentCaptor<Label> newLabel = ArgumentCaptor.forClass(Label.class);
    verify(repository, times(1)).save(newLabel.capture());
    assertThat(newLabel.getValue().getLabelKey()).isEqualTo(label.getLabelKey());
    assertThat(newLabel.getValue().getLanguageCode()).isEqualTo(label.getLanguageCode());
    assertThat(newLabel.getValue().getLabelValue()).isEqualTo(label.getLabelValue());
    assertThat(newLabel.getValue().getField()).isEqualTo(label.getField());
  }


  @Test
  void testCreateOrUpdateWithExistingDataFoundByIdWithoutChange() {
    Label label = newLabel();
    when(repository.findAll()).thenReturn(List.of(label));
    service.createOrUpdate(label.getLanguageCode(), label.getLabelKey(), label.getLabelValue(), label.getField());
    verify(repository, times(0)).save(label);
  }

  @Test
  void testCreateOrUpdateWithExistingDataFoundByIdWithChangedValue() {
    Label label = newLabel();
    when(repository.findAll()).thenReturn(List.of(label));
    when(repository.findByLanguageCodeAndLabelKey(label.getLanguageCode(), label.getLabelKey())).thenReturn(Optional.of(label));
    service.createOrUpdate(label.getLanguageCode(), label.getLabelKey(), "changedValue", label.getField());
    verify(repository, times(1)).save(label);
  }

  @Test
  void testCreateOrUpdateWithExistingDataFoundByIdWithChangedGroup() {
    Label label = newLabel();
    when(repository.findAll()).thenReturn(List.of(label));
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

  @Test
  void testFindByLanguageAndField() {
    Label label = newLabel();
    when(repository.findAll()).thenReturn(List.of(label));
    Map<String, String> result = service.findByLanguageAndField(label.getLanguageCode(), label.getField());
    assertThat(result).isNotNull()
      .containsEntry(label.getLabelKey(), label.getLabelValue());
    result = service.findByLanguageAndField(label.getLanguageCode(), label.getField());
    assertThat(result).isNotNull()
      .containsEntry(label.getLabelKey(), label.getLabelValue());
  }

}

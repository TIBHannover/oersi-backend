package org.oersi.service;

import org.junit.jupiter.api.Test;
import org.oersi.domain.Label;
import org.oersi.repository.LabelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

import static org.mockito.Mockito.*;

@SpringBootTest
class LabelServiceTest {

  @Autowired
  private LabelService service;
  @MockBean
  private LabelRepository repository;

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
  void testCreateOrUpdateWithExistingDataFoundById() {
    Label label = newLabel();
    when(repository.findByLanguageCodeAndLabelKey(label.getLanguageCode(), label.getLabelKey())).thenReturn(Optional.of(label));
    service.createOrUpdate(label.getLanguageCode(), label.getLabelKey(), label.getLabelValue(), label.getGroupId());
    verify(repository, times(1)).save(label);
  }

}

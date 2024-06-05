package org.sidre.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sidre.ElasticsearchServicesMock;
import org.sidre.domain.VocabItem;
import org.sidre.repository.VocabItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@Import(ElasticsearchServicesMock.class)
class VocabServiceTest {

  @Autowired
  private VocabService service;
  @Autowired
  private VocabItemRepository repository; // mock from ElasticsearchServicesMock
  @MockBean
  private JavaMailSender mailSender;

  @BeforeEach
  void cleanup() {
    ((VocabServiceImpl)service).clearCache();
  }

  @AfterEach
  void tearDown() {
    ((VocabServiceImpl)service).clearCache();
  }

  private VocabItem getTestData() {
    VocabItem definition = new VocabItem();
    definition.setVocabIdentifier("ABC");
    definition.setItemKey("XXX");
    Map<String, String> localizedStrings = new HashMap<>();
    localizedStrings.put("de", "test1");
    localizedStrings.put("en", "test2");
    definition.setPrefLabel(localizedStrings);
    return definition;
  }

  @Test
  void testCreateOrUpdateWithoutExistingData() {
    List<VocabItem> data = List.of(getTestData());
    when(repository.findAll()).thenReturn(List.of());
    service.updateVocab("ABC", data);
    verify(repository, times(1)).saveAll(data);
  }

  @Test
  void testCreateOrUpdateWithExistingData() {
    List<VocabItem> existing = List.of(getTestData());
    when(repository.findAll()).thenReturn(existing);
    List<VocabItem> data = new ArrayList<>();
    data.add(getTestData());
    data.get(0).setItemKey("ABC");
    data.addAll(existing);
    service.updateVocab("ABC", data);
    verify(repository, times(1)).saveAll(data);
  }

  @Test
  void testFindLocalizedLabelWithIllegalId() {
    Map<String, String> result = service.findLocalizedLabelByIdentifier(null);
    assertThat(result).isNull();
  }

  @Test
  void testFindLocalizedLabelWithUndefinedLocalizedString() {
    List<VocabItem> existing = List.of(getTestData());
    existing.get(0).setPrefLabel(new HashMap<>());
    when(repository.findAll()).thenReturn(existing);
    Map<String, String> result = service.findLocalizedLabelByIdentifier("XXX");
    assertThat(result).isEmpty();

    existing.get(0).setPrefLabel(null);
    when(repository.findAll()).thenReturn(existing);
    result = service.findLocalizedLabelByIdentifier("XXX");
    assertThat(result).isEmpty();
  }

  @Test
  void testFindLocalizedLabel() {
    List<VocabItem> existing = List.of(getTestData());
    when(repository.findAll()).thenReturn(existing);
    Map<String, String> result = service.findLocalizedLabelByIdentifier("XXX");
    assertThat(result).hasSize(2).containsKeys("de", "en");
    result = service.findLocalizedLabelByIdentifier("XXX"); // second time for cached result
    assertThat(result).hasSize(2).containsKeys("de", "en");
  }

  @Test
  void testFindLocalizedLabelAndUpdate() {
    List<VocabItem> existing = List.of(getTestData());
    when(repository.findAll()).thenReturn(existing);
    Map<String, String> result = service.findLocalizedLabelByIdentifier("XXX");
    assertThat(result).hasSize(2).containsKeys("de", "en").containsEntry("de", "test1");

    List<VocabItem> newData = List.of(getTestData());
    newData.get(0).setPrefLabel(Map.of("de", "test3"));
    service.updateVocab(newData.get(0).getVocabIdentifier(), newData);
    when(repository.findAll()).thenReturn(newData);
    result = service.findLocalizedLabelByIdentifier("XXX"); // second time for cached result
    assertThat(result).hasSize(1).containsEntry("de", "test3");
  }

  @Test
  void testGetParentMap() {
    List<VocabItem> vocabItems = new ArrayList<>();
    VocabItem item1 = new VocabItem();
    item1.setParentKey("hochschulfaechersystematik");
    item1.setItemKey("https://w3id.org/kim/hochschulfaechersystematik/n009");
    item1.setParentKey("https://w3id.org/kim/hochschulfaechersystematik/n42");
    vocabItems.add(item1);
    VocabItem item2 = new VocabItem();
    item2.setParentKey("hochschulfaechersystematik");
    item2.setItemKey("https://w3id.org/kim/hochschulfaechersystematik/n42");
    item2.setParentKey("https://w3id.org/kim/hochschulfaechersystematik/n4");
    vocabItems.add(item2);
    when(repository.findByVocabIdentifier("hochschulfaechersystematik")).thenReturn(vocabItems);
    var result = service.getParentMap("hochschulfaechersystematik");
    assertThat(result).hasSize(2);
  }

}

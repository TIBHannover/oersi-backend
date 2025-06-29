package org.sidre.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sidre.ElasticsearchServicesMock;
import org.sidre.domain.BackendConfig;
import org.sidre.domain.VocabItem;
import org.sidre.repository.VocabItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@SpringBootTest
@ElasticsearchServicesMock
class LabelServiceTest {

  @MockitoBean
  private ConfigService configService;
  @Autowired
  private LabelService service;
  @Autowired
  private VocabItemRepository repository; // mock from ElasticsearchServicesMock
  @MockitoBean
  private JavaMailSender mailSender;

  @BeforeEach
  void setup() {
    service.clearCache();
    BackendConfig initialConfig = new BackendConfig();
    BackendConfig.FieldProperties aboutProperties = new BackendConfig.FieldProperties();
    aboutProperties.setFieldName("about");
    aboutProperties.setVocabIdentifier("about");
    initialConfig.setFieldProperties(List.of(aboutProperties));
    when(configService.getMetadataConfig()).thenReturn(initialConfig);
  }

  @AfterEach
  void tearDown() {
    service.clearCache();
  }

  private VocabItem newLabelVocabItem() {
    VocabItem definition = new VocabItem();
    definition.setVocabIdentifier("about");
    definition.setItemKey("key");
    Map<String, String> localizedStrings = new HashMap<>();
    localizedStrings.put("en", "value");
    definition.setPrefLabel(localizedStrings);
    return definition;
  }

  @Test
  void testFindByLanguage() {
    VocabItem item = newLabelVocabItem();
    when(repository.findAll()).thenReturn(List.of(item));
    Map<String, String> result = service.findByLanguage("en");
    assertThat(result).isNotNull().containsExactlyEntriesOf(Map.of("key", "value"));
    result = service.findByLanguage("en");
    assertThat(result).isNotNull().containsExactlyEntriesOf(Map.of("key", "value"));
  }

  @Test
  void testFindByLanguageAndField() {
    VocabItem item = newLabelVocabItem();
    when(repository.findAll()).thenReturn(List.of(item));
    Map<String, String> result = service.findByLanguageAndField("en", "about");
    assertThat(result).isNotNull().containsExactlyEntriesOf(Map.of("key", "value"));
    result = service.findByLanguageAndField("en", "about");
    assertThat(result).isNotNull().containsExactlyEntriesOf(Map.of("key", "value"));
  }

  @Test
  void testFindByLanguageAndFieldWithoutFieldVocabRelationship() {
    reset(configService);
    when(configService.getMetadataConfig()).thenReturn(null);
    VocabItem item = newLabelVocabItem();
    when(repository.findAll()).thenReturn(List.of(item));
    Map<String, String> result = service.findByLanguageAndField("en", "about");
    assertThat(result).isNotNull().isEmpty();
    result = service.findByLanguageAndField("en", "about");
    assertThat(result).isNotNull().isEmpty();
  }

}

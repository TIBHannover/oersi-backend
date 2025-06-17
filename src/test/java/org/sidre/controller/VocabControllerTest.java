package org.sidre.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sidre.ElasticsearchContainerTest;
import org.sidre.dto.LocalizedStringDto;
import org.sidre.dto.VocabBulkBodyDto;
import org.sidre.dto.VocabItemDto;
import org.sidre.repository.VocabItemRepository;
import org.sidre.service.VocabService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WithMockUser(roles = {"MANAGE_METADATA"})
class VocabControllerTest extends ElasticsearchContainerTest {

  private static final String CONTROLLER_BASE_PATH = "/api/vocab";

  @Autowired
  private MockMvc mvc;

  @Autowired
  private VocabItemRepository repository;
  @Autowired
  private VocabService vocabService;
  @MockitoBean
  private JavaMailSender mailSender;

  @BeforeEach
  void cleanup() {
    repository.deleteAll();
  }

  private static String asJson(final Object obj) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.writeValueAsString(obj);
  }

  @Test
  void testCreateOrUpdateMany() throws Exception {
    VocabBulkBodyDto body = new VocabBulkBodyDto();
    body.setVocabIdentifier("hochschulfaechersystematik");
    List<VocabItemDto> vocabItems = new ArrayList<>();
    VocabItemDto item1 = new VocabItemDto();
    item1.setKey("key1");
    item1.setParentKey("key2");
    item1.setPrefLabels(new LocalizedStringDto());
    item1.getPrefLabels().put("en", "test");
    vocabItems.add(item1);
    body.setItems(vocabItems);
    mvc.perform(post(CONTROLLER_BASE_PATH + "/bulk")
      .contentType(MediaType.APPLICATION_JSON)
      .content(asJson(body))).andExpect(status().isOk());
    Assertions.assertEquals(1, repository.count());
  }

}

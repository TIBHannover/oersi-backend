package org.oersi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oersi.ElasticsearchServicesMock;
import org.oersi.dto.VocabBulkBodyDto;
import org.oersi.dto.VocabItemDto;
import org.oersi.repository.VocabItemRepository;
import org.oersi.service.VocabService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles = {"MANAGE_OERMETADATA"})
@Import(ElasticsearchServicesMock.class)
class VocabControllerTest {

  private static final String CONTROLLER_BASE_PATH = "/api/vocab";

  @Autowired
  private MockMvc mvc;

  @Autowired
  private VocabItemRepository repository;
  @Autowired
  private VocabService vocabService;
  @MockBean
  private JavaMailSender mailSender;

  @BeforeEach
  void cleanup() {
    repository.deleteAll();
    repository.flush();
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
    vocabItems.add(item1);
    body.setItems(vocabItems);
    mvc.perform(post(CONTROLLER_BASE_PATH + "/bulk")
      .contentType(MediaType.APPLICATION_JSON)
      .content(asJson(body))).andExpect(status().isOk());
  }

}

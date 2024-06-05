package org.sidre.controller;

import org.junit.jupiter.api.Test;
import org.sidre.ElasticsearchServicesMock;
import org.sidre.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(ElasticsearchServicesMock.class)
class ContactControllerTest {

  private static final String CONTACT_CONTROLLER_BASE_PATH = "/api/contact";

  @Autowired
  private MockMvc mvc;

  @Autowired
  private ContactService contactService;

  @MockBean
  private JavaMailSender mailSender;

  @Test
  void testPostContact() throws Exception {
    mvc.perform(post(CONTACT_CONTROLLER_BASE_PATH).contentType(MediaType.APPLICATION_JSON)
      .content("{\"email\": \"testmail@test.org\", \"message\": \"testmessage\", \"subject\": \"testsubject\"}"))
      .andExpect(status().isOk());
  }
}

package org.oersi.controller;

import org.junit.jupiter.api.Test;
import org.oersi.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
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
    mvc.perform(post(CONTACT_CONTROLLER_BASE_PATH).contentType(MediaType.APPLICATION_FORM_URLENCODED)
      .content(buildUrlEncodedFormEntity("email", "testmail@test.org", "message", "testmessage", "subject", "testsubject")))
      .andExpect(status().isOk());
  }

  private String buildUrlEncodedFormEntity(String... params) {
    if( (params.length % 2) > 0 ) {
      throw new IllegalArgumentException("Need to give an even number of parameters");
    }
    StringBuilder result = new StringBuilder();
    for (int i=0; i<params.length; i+=2) {
      if( i > 0 ) {
        result.append('&');
      }
      try {
        result.
          append(URLEncoder.encode(params[i], StandardCharsets.UTF_8.name())).
          append('=').
          append(URLEncoder.encode(params[i+1], StandardCharsets.UTF_8.name()));
      }
      catch (UnsupportedEncodingException e) {
        throw new RuntimeException(e);
      }
    }
    return result.toString();
  }
}

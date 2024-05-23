package org.oersi.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@PropertySource(value = "file:${envConfigDir:envConf/default/}search_index.properties")
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ContactServiceImpl implements ContactService {

  private final @NonNull JavaMailSender mailSender;

  @Value("${oersi.support.mail}")
  private String supportMail;

  @Override
  public void createContactRequest(String email, String subject, String message) {
    var mailMessage = new SimpleMailMessage();
    mailMessage.setFrom(email);
    mailMessage.setTo(supportMail);
    mailMessage.setSubject(subject);
    mailMessage.setText(message);
    mailSender.send(mailMessage);
  }
}

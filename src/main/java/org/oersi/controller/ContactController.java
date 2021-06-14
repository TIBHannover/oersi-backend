package org.oersi.controller;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oersi.api.ContactControllerApi;
import org.oersi.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ContactController implements ContactControllerApi {

  private final @NonNull ContactService contactService;

  @Override
  public ResponseEntity<Void> contact(String email, String subject, String message) {
    log.info("Incoming contact request {} {}", email, subject);
    contactService.createContactRequest(email, subject, message);
    return ResponseEntity.ok().build();
  }

}

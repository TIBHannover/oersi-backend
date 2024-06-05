package org.sidre.controller;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sidre.api.ContactControllerApi;
import org.sidre.dto.ContactRequestDto;
import org.sidre.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ContactController implements ContactControllerApi {

  private final @NonNull ContactService contactService;

  @Override
  public ResponseEntity<Void> contact(ContactRequestDto contactRequest) {
    log.info("Incoming contact request {} {}", contactRequest.getEmail(), contactRequest.getSubject());
    contactService.createContactRequest(contactRequest.getEmail(), contactRequest.getSubject(), contactRequest.getMessage());
    return ResponseEntity.ok().build();
  }

}

package org.sidre.controller;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.MappingException;
import org.modelmapper.spi.ErrorMessage;
import org.springframework.http.ResponseEntity;

@Slf4j
public class ControllerUtil {
  private ControllerUtil() {}

  public static ResponseEntity<String> handleMappingException(final MappingException e) {
    final StringBuilder resultMsg = new StringBuilder();
    for (ErrorMessage errorMessage : e.getErrorMessages()) {
      resultMsg.append(errorMessage.getMessage());
      if (errorMessage.getCause() != null) {
        resultMsg.append(" - ").append(errorMessage.getCause().getMessage());
      }
      resultMsg.append(", ");
    }
    log.debug("Mapping exception: {}", resultMsg);
    return ResponseEntity.badRequest().body(resultMsg.toString());
  }
}

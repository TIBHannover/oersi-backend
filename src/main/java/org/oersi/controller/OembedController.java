package org.oersi.controller;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oersi.api.OembedControllerApi;
import org.oersi.dto.OembedResponseDto;
import org.oersi.service.OembedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OembedController implements OembedControllerApi {

  private final @NonNull OembedService oembedService;

  @Override
  public ResponseEntity<OembedResponseDto> oembed(String url, Integer maxwidth, Integer maxheight) {
    var oembed = oembedService.getOembedResponse(url, maxwidth, maxheight);
    if (oembed == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(oembed);
  }

  @Override
  public ResponseEntity<OembedResponseDto> oembedXmlWrapper(String url, Integer maxwidth, Integer maxheight) {
    return oembed(url, maxwidth, maxheight);
  }

}

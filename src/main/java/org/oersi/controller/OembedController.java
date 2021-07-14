package org.oersi.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oersi.api.OembedControllerApi;
import org.oersi.dto.OembedResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OembedController implements OembedControllerApi {

  @Override
  public ResponseEntity<OembedResponseDto> oembed(String url, Integer maxwidth, Integer maxheight) {
    var resp = new OembedResponseDto();

    return ResponseEntity.ok(resp);
  }

  @Override
  public ResponseEntity<OembedResponseDto> oembedXmlWrapper(String url, Integer maxwidth, Integer maxheight) {
    return oembed(url, maxwidth, maxheight);
  }

}

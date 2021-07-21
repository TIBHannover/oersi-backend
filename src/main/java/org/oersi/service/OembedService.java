package org.oersi.service;

import org.oersi.dto.OembedResponseDto;

public interface OembedService {

  /**
   * Get the embedding information for the given URL in format oEmbed.
   * @param url The URL to retrieve embedding information for.
   * @param maxwidth The maximum width of the embedded resource. Only applies to some resource types (as specified below). For supported resource types, this parameter must be respected by providers.
   * @param maxheight The maximum height of the embedded resource. Only applies to some resource types (as specified below). For supported resource types, this parameter must be respected by providers.
   * @return embedding information
   */
  OembedResponseDto getOembedResponse(String url, Integer maxwidth, Integer maxheight);

}

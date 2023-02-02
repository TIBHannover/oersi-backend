package org.oersi.domain;

import lombok.Data;

import java.util.List;

@Data
public class OembedInfo {

  @Data
  public static class Author {
    private String name;
    private String url;
  }

  private String title;
  private String providerName;
  private String providerUrl;
  private List<Author> authors;
  private String licenseUrl;
  private String thumbnailUrl;
  private String videoEmbedUrl;

}

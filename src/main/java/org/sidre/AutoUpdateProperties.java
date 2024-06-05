package org.sidre;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Definition of automatic update of missing information
 */
@ConfigurationProperties(prefix = "autoupdate")
@Data
public class AutoUpdateProperties {

  @Data
  public static class Entry {
    /**
     * Update data whose identifier matches this regex
     */
    private String regex;
    private String embedUrl;
    private String providerName;
    private String providerUrl;
  }

  private List<Entry> definitions;

}

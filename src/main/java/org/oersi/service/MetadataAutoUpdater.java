package org.oersi.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.oersi.AutoUpdateInfo;
import org.oersi.AutoUpdateProperties;
import org.oersi.domain.BackendMetadata;
import org.oersi.domain.OembedInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class that sets missing infos at {@link org.oersi.domain.BackendMetadata}
 */
@Service
@PropertySource(value = "file:${envConfigDir:envConf/default/}oersi.properties")
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Setter
public class MetadataAutoUpdater {

  private final @NonNull AutoUpdateProperties autoUpdateProperties;
  private final @NonNull MetadataFieldService metadataFieldService;

  public void initAutoUpdateInfo(BackendMetadata data) {
    AutoUpdateInfo info = new AutoUpdateInfo();
    for (AutoUpdateProperties.Entry definition : autoUpdateProperties.getDefinitions()) {
      var pattern = Pattern.compile(definition.getRegex());
      var id = metadataFieldService.getIdentifier(data.getData());
      var matcher = pattern.matcher(id);
      if (matcher.matches()) {
        info.setEmbedUrl(getEmbedUrl(id, definition, matcher));
        info.setProviderName(definition.getProviderName());
        info.setProviderUrl(definition.getProviderUrl());
        break;
      }
    }
    data.setAutoUpdateInfo(info);
  }

  public OembedInfo initOembedInfo(BackendMetadata data) {
    AutoUpdateInfo info = data.getAutoUpdateInfo();
    OembedInfo oembedInfo = new OembedInfo();
    oembedInfo.setProviderName(info.getProviderName());
    oembedInfo.setProviderUrl(info.getProviderUrl());
    oembedInfo.setVideoEmbedUrl(info.getEmbedUrl());
    return oembedInfo;
  }

  private String getEmbedUrl(String id, AutoUpdateProperties.Entry definition, Matcher matcher) {
    var embedUrl = definition.getEmbedUrl();
    if (embedUrl == null) {
      return null; // no embed url in this definition -> skip
    }
    log.debug("{} matches {}", definition.getRegex(), id);
    for (int i = 1; i <= matcher.groupCount(); i++) {
      embedUrl = embedUrl.replace("###" + i + "###", matcher.group(i));
    }
    log.debug("embed url {}", embedUrl);
    return embedUrl;
  }

}

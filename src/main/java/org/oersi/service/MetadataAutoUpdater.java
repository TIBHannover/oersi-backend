package org.oersi.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.oersi.AutoUpdateProperties;
import org.oersi.domain.Media;
import org.oersi.domain.Metadata;
import org.oersi.domain.Provider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class that sets missing infos at {@link org.oersi.domain.Metadata} (like embed-url of known videos, image width and height, provider)
 */
@Service
@PropertySource(value = "file:${envConfigDir:envConf/default/}oersi.properties")
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Setter
public class MetadataAutoUpdater {

  public interface ImageLoader {
    BufferedImage getImage(String source) throws IOException;
  }

  public static class UrlImageLoader implements ImageLoader {
    @Override
    public BufferedImage getImage(String source) throws IOException {
      return ImageIO.read(new URL(source));
    }
  }

  private final @NonNull AutoUpdateProperties autoUpdateProperties;
  private ImageLoader imageLoader = new UrlImageLoader();

  public void addMissingInfos(Metadata data) {
    setImageDimensions(data);

    boolean shouldUpdateByDefinitions = !hasEmbedUrl(data) || !hasProviderName(data) || !hasProviderUrl(data);
    if (shouldUpdateByDefinitions) {
      for (AutoUpdateProperties.Entry definition : autoUpdateProperties.getDefinitions()) {
        var pattern = Pattern.compile(definition.getRegex());
        var matcher = pattern.matcher(data.getIdentifier());
        if (matcher.matches()) {
          updateMissingEmbedUrl(data, definition, matcher);
          updateMissingProviderInfo(data, definition);
        }
      }
    }
  }

  private void updateMissingProviderInfo(Metadata data, AutoUpdateProperties.Entry definition) {
    boolean existingProviderName = hasProviderName(data);
    boolean existingProviderUrl = hasProviderUrl(data);
    if (existingProviderName && existingProviderUrl) {
      return; // provider infos already there
    }
    Provider provider = data.getProvider();
    if (provider == null) {
      provider = new Provider();
      data.setProvider(provider);
    }
    if (definition.getProviderName() != null && !existingProviderName) {
      provider.setName(definition.getProviderName());
      log.debug("provider name {}", provider.getName());
    }
    if (definition.getProviderUrl() != null && !existingProviderUrl) {
      provider.setIdentifier(definition.getProviderUrl());
      log.debug("provider url {}", provider.getIdentifier());
    }
  }

  private void updateMissingEmbedUrl(Metadata data, AutoUpdateProperties.Entry definition, Matcher matcher) {
    if (hasEmbedUrl(data)) {
      return; // embed url already set -> skip
    }
    var embedUrl = definition.getEmbedUrl();
    if (embedUrl == null) {
      return; // no embed url in this definition -> skip
    }
    log.debug("{} matches {}", definition.getRegex(), data.getIdentifier());
    for (int i = 1; i <= matcher.groupCount(); i++) {
      embedUrl = embedUrl.replace("###" + i + "###", matcher.group(i));
    }
    log.debug("embed url {}", embedUrl);
    List<Media> encoding = data.getEncoding();
    if (encoding == null) {
      encoding = new ArrayList<>();
      data.setEncoding(encoding);
    }
    Media videoEncoding = new Media();
    videoEncoding.setEmbedUrl(embedUrl);
    encoding.add(videoEncoding);
  }

  private void setImageDimensions(Metadata data) {
    if (data.getImage() != null && (data.getImageWidth() == null || data.getImageHeight() == null)) {
      log.debug("Set dimensions of image {}", data.getImage());
      try {
        BufferedImage image = imageLoader.getImage(data.getImage());
        if (image == null) {
          log.info("Could not read image {}", data.getImage());
        } else {
          if (data.getImageWidth() == null) {
            data.setImageWidth(image.getWidth());
            log.debug("image width {}", data.getImageWidth());
          }
          if (data.getImageHeight() == null) {
            data.setImageHeight(image.getHeight());
            log.debug("image height {}", data.getImageHeight());
          }
        }
      } catch (IOException e) {
        log.debug("error while reading image", e);
        log.info("Could not read image {}", data.getImage());
      }
    }
  }

  private boolean hasProviderName(Metadata data) {
    return data.getProvider() != null && data.getProvider().getName() != null;
  }
  private boolean hasProviderUrl(Metadata data) {
    return data.getProvider() != null && data.getProvider().getIdentifier() != null;
  }
  private boolean hasEmbedUrl(Metadata data) {
    return data.getEncoding() != null && data.getEncoding().stream().anyMatch(e -> e.getEmbedUrl() != null);
  }

}

package org.sidre.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.sidre.domain.BackendMetadata;
import org.sidre.domain.OembedInfo;
import org.sidre.dto.OembedResponseAuthorsDto;
import org.sidre.dto.OembedResponseDto;
import org.sidre.repository.MetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Setter
public class OembedServiceImpl implements OembedService {

  public interface ImageLoader {
    BufferedImage getImage(String source) throws IOException;
  }

  public static class UrlImageLoader implements ImageLoader {
    @Override
    public BufferedImage getImage(String source) throws IOException {
      return ImageIO.read(new URL(source));
    }
  }
  private ImageLoader imageLoader = new UrlImageLoader();

  private final @NonNull MetadataRepository metadataRepository;

  @Override
  public OembedResponseDto getOembedResponse(String url, Integer maxWidth, Integer maxHeight) {
    var data = findMetadataForOembedUrl(url);
    if (data == null) {
      return null;
    }
    OembedInfo oembedInfo = data.getOembedInfo();
    if (oembedInfo == null) {
      return null;
    }
    var oembed = new OembedResponseDto();
    oembed.setType(OembedResponseDto.TypeEnum.LINK);
    oembed.setTitle(oembedInfo.getTitle());
    if (!CollectionUtils.isEmpty(oembedInfo.getAuthors())) {
      List<OembedResponseAuthorsDto> authors = oembedInfo.getAuthors().stream().map(this::convertToOembedAuthor).collect(Collectors.toList());
      oembed.setAuthors(authors);
      if (authors.stream().anyMatch(a -> a.getName() != null)) {
        oembed.setAuthorName(authors.stream().map(OembedResponseAuthorsDto::getName).filter(Objects::nonNull).collect(Collectors.joining(", ")));
      }
      if (authors.stream().anyMatch(a -> a.getUrl() != null)) {
        oembed.setAuthorUrl(authors.stream().map(OembedResponseAuthorsDto::getUrl).filter(Objects::nonNull).collect(Collectors.joining(",")));
      }
    }
    oembed.setProviderName(oembedInfo.getProviderName());
    oembed.setProviderUrl(oembedInfo.getProviderUrl());
    oembed.setLicenseUrl(oembedInfo.getLicenseUrl());

    setThumbnailFields(oembedInfo, oembed, maxWidth, maxHeight);
    setVideoFieldsIfIsVideo(oembedInfo, oembed, maxWidth, maxHeight);
    return oembed;
  }

  private void setThumbnailFields(OembedInfo data, OembedResponseDto oembed, Integer maxWidth, Integer maxHeight) {
    String image = data.getThumbnailUrl();
    if (image != null) {
      try {
        log.debug("Get dimensions of image {}", image);
        BufferedImage bufferedImage = imageLoader.getImage(image);
        if (bufferedImage == null) {
          log.info("Could not read image {}", image);
        } else {
          int imageWidth = bufferedImage.getWidth();
          int imageHeight = bufferedImage.getHeight();
          boolean widthMatches = maxWidth == null || imageWidth <= maxWidth;
          boolean heightMatches = maxHeight == null || imageHeight <= maxHeight;
          if (widthMatches && heightMatches) {
            oembed.setThumbnailUrl(image);
            oembed.setThumbnailWidth(imageWidth);
            oembed.setThumbnailHeight(imageHeight);
          }
        }
      } catch (IOException e) {
        log.debug("error while reading image", e);
        log.info("Could not read image {}", image);
      }
    }
  }

  /**
   * Set fields 'html', 'width', 'height' if the given {@link OembedInfo} represents a video at the given {@link OembedResponseDto}.
   * @param data metadata record
   * @param oembed set video fields here
   * @param maxwidth max width of the video
   * @param maxheight max height of the video
   */
  private void setVideoFieldsIfIsVideo(OembedInfo data, OembedResponseDto oembed, Integer maxwidth, Integer maxheight) {
    if (data.getVideoEmbedUrl() != null) {
      oembed.setType(OembedResponseDto.TypeEnum.VIDEO);
      setVideoWidthAndHeight(oembed, maxwidth, maxheight);
      StringBuilder html = new StringBuilder();
      html.append("<iframe width=\"").append(oembed.getWidth());
      html.append("\" height=\"").append(oembed.getHeight());
      html.append("\" src=\"").append(data.getVideoEmbedUrl());
      html.append("\" frameborder=\"0\" allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture\" allowfullscreen></iframe>");
      oembed.setHtml(html.toString());
    }
  }

  private void setVideoWidthAndHeight(OembedResponseDto oembed, Integer maxwidth, Integer maxheight) {
    final double ratio = 0.5625; // aspect ratio for video
    int width = 560; // default width
    int height = 315; // default height
    if (maxwidth != null) {
      width = Math.min(maxwidth, width);
    }
    if (maxheight != null) {
      height = Math.min(maxheight, height);
    }
    if (width * ratio < height) {
      height = (int) Math.floor(width * ratio);
    } else {
      width = (int) Math.floor(height / ratio);
    }
    oembed.setWidth(width);
    oembed.setHeight(height);
  }
  private BackendMetadata findMetadataForOembedUrl(String url) {
    var existingMetadata = findMetadataByIdentifier(url);
    if (existingMetadata == null) {
      // try to extract base64-encoded identifier from URL
      var pattern = Pattern.compile(".*/([A-Za-z0-9-_=]+)$");
      var matcher = pattern.matcher(url);
      if (matcher.matches()) {
        var encodedUrl = matcher.group(1);
        existingMetadata = findMetadataByIdentifier(encodedUrl);
      }
    }
    return existingMetadata;
  }

  private BackendMetadata findMetadataByIdentifier(String identifier) {
    String searchId;
    if (identifier.matches("[A-Za-z0-9-_=]+")) {
      searchId = identifier;
    } else {
      searchId = Base64.getUrlEncoder().encodeToString(identifier.getBytes(StandardCharsets.UTF_8));
    }
    return metadataRepository.findById(searchId).orElse(null);
  }

  private OembedResponseAuthorsDto convertToOembedAuthor(OembedInfo.Author creator) {
    var author = new OembedResponseAuthorsDto();
    author.setName(creator.getName());
    author.setUrl(creator.getUrl());
    return author;
  }

}

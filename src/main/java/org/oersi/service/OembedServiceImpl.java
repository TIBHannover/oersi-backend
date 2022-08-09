package org.oersi.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.oersi.domain.Creator;
import org.oersi.domain.Media;
import org.oersi.domain.Metadata;
import org.oersi.dto.OembedResponseAuthorsDto;
import org.oersi.dto.OembedResponseDto;
import org.oersi.repository.MetadataRepository;
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

  private final @NonNull MetadataRepository metadataRepository;

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

  @Override
  public OembedResponseDto getOembedResponse(String url, Integer maxwidth, Integer maxheight) {
    var data = findMetadataForOembedUrl(url);
    if (data == null) {
      return null;
    }
    var oembed = new OembedResponseDto();
    oembed.setType(OembedResponseDto.TypeEnum.LINK);
    oembed.setTitle(data.getName());
    if (!CollectionUtils.isEmpty(data.getCreator())) {
      List<OembedResponseAuthorsDto> authors = data.getCreator().stream().map(this::convertToOembedAuthor).collect(Collectors.toList());
      oembed.setAuthors(authors);
      if (authors.stream().anyMatch(a -> a.getName() != null)) {
        oembed.setAuthorName(authors.stream().map(OembedResponseAuthorsDto::getName).filter(Objects::nonNull).collect(Collectors.joining(", ")));
      }
      if (authors.stream().anyMatch(a -> a.getUrl() != null)) {
        oembed.setAuthorUrl(authors.stream().map(OembedResponseAuthorsDto::getUrl).filter(Objects::nonNull).collect(Collectors.joining(",")));
      }
    }
    if (data.getProvider() != null) {
      oembed.setProviderName(data.getProvider().getName());
      oembed.setProviderUrl(data.getProvider().getIdentifier());
    }
    if (data.getLicense() != null) {
      oembed.setLicenseUrl(data.getLicense().getIdentifier());
    }

    setThumbnailFields(data, oembed, maxwidth, maxheight);
    setVideoFieldsIfIsVideo(data, oembed, maxwidth, maxheight);
    return oembed;
  }

  private void setThumbnailFields(Metadata data, OembedResponseDto oembed, Integer maxWidth, Integer maxHeight) {
    if (data.getImage() != null) {
      try {
        log.debug("Get dimensions of image {}", data.getImage());
        BufferedImage image = imageLoader.getImage(data.getImage());
        if (image == null) {
          log.info("Could not read image {}", data.getImage());
        } else {
          int imageWidth = image.getWidth();
          int imageHeight = image.getHeight();
          boolean widthMatches = maxWidth == null || imageWidth <= maxWidth;
          boolean heightMatches = maxHeight == null || imageHeight <= maxHeight;
          if (widthMatches && heightMatches) {
            oembed.setThumbnailUrl(data.getImage());
            oembed.setThumbnailWidth(imageWidth);
            oembed.setThumbnailHeight(imageHeight);
          }
        }
      } catch (IOException e) {
        log.debug("error while reading image", e);
        log.info("Could not read image {}", data.getImage());
      }
    }
  }

  /**
   * Set fields 'html', 'width', 'height' if the given {@link Metadata} represents a video at the given {@link OembedResponseDto}.
   * @param data metadata record
   * @param oembed set video fields here
   * @param maxwidth max width of the video
   * @param maxheight max height of the video
   * @return true, if it is a video and the field were set; false, otherwise
   */
  private boolean setVideoFieldsIfIsVideo(Metadata data, OembedResponseDto oembed, Integer maxwidth, Integer maxheight) {
    if (!CollectionUtils.isEmpty(data.getEncoding())) {
      List<Media> videoEmbeddings = data.getEncoding().stream()
        .filter(e -> e.getEmbedUrl() != null)
        .collect(Collectors.toList());
      if (!videoEmbeddings.isEmpty()) {
        oembed.setType(OembedResponseDto.TypeEnum.VIDEO);
        setVideoWidthAndHeight(oembed, maxwidth, maxheight);
        String embedUrl = videoEmbeddings.get(0).getEmbedUrl();
        StringBuilder html = new StringBuilder();
        html.append("<iframe width=\"").append(oembed.getWidth());
        html.append("\" height=\"").append(oembed.getHeight());
        html.append("\" src=\"").append(embedUrl);
        html.append("\" frameborder=\"0\" allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture\" allowfullscreen></iframe>");
        oembed.setHtml(html.toString());
        return true;
      }
    }
    return false;
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

  private OembedResponseAuthorsDto convertToOembedAuthor(Creator creator) {
    var author = new OembedResponseAuthorsDto();
    author.setName(creator.getName());
    author.setUrl(creator.getIdentifier());
    return author;
  }

  private Metadata findMetadataForOembedUrl(String url) {
    var existingMetadata = findMetadataByIdentifier(url);
    if (existingMetadata == null) {
      // try to extract base64-encoded identifier from URL
      var pattern = Pattern.compile(".*/([A-Za-z0-9-_=]+)$");
      var matcher = pattern.matcher(url);
      if (matcher.matches()) {
        var encodedUrl = matcher.group(1);
        var decodedUrl = new String(Base64.getUrlDecoder().decode(encodedUrl.getBytes(StandardCharsets.UTF_8)));
        log.debug("Using base64 urlsafe decoded identifier {}", decodedUrl);
        existingMetadata = findMetadataByIdentifier(decodedUrl);
      }
    }
    return existingMetadata;
  }

  private Metadata findMetadataByIdentifier(String identifier) {
    Metadata existingMetadata = null;
    List<Metadata> metadataMatchingUrl = metadataRepository.findByIdentifier(identifier);
    if (!metadataMatchingUrl.isEmpty()) {
      existingMetadata = metadataMatchingUrl.get(0);
    }
    return existingMetadata;
  }
}

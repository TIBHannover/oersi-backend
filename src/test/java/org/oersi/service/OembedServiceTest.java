package org.oersi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.oersi.domain.Creator;
import org.oersi.domain.License;
import org.oersi.domain.Media;
import org.oersi.domain.Metadata;
import org.oersi.domain.Provider;
import org.oersi.dto.OembedResponseDto;
import org.oersi.repository.MetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
class OembedServiceTest {

  @Autowired
  private OembedService service;
  @MockBean
  private MetadataRepository repository;

  @BeforeEach
  void init() {
    ((OembedServiceImpl)service).setImageLoader(new OembedServiceImpl.ImageLoader() {
      @Override
      public BufferedImage getImage(String source) throws IOException {
        if ("null".equals(source)) {
          return null;
        }
        if ("ioexception".equals(source)) {
          throw new IOException();
        }
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(source)) {
          return ImageIO.read(is);
        }
      }
    });
  }

  private Metadata newMetadata() {
    Metadata metadata = new Metadata();
    metadata.setRecordStatusInternal(Metadata.RecordStatus.ACTIVE);

    List<Creator> creators = new ArrayList<>();
    Creator author = new Creator();
    author.setType("Person");
    author.setName("test test");
    creators.add(author);

    metadata.setCreator(creators);

    License license = new License();
    license.setIdentifier("https://creativecommons.org/licenses/by/4.0/");
    metadata.setLicense(license);
    metadata.setName("Test Title");
    metadata.setIdentifier("https://www.test.de");

    return metadata;
  }

  @Test
  void testVideoWidthAndHeight() {
    Metadata dummyData = newMetadata();
    Media videoEncoding = new Media();
    videoEncoding.setEncodingFormat("video/mp4");
    videoEncoding.setEmbedUrl("https://embed.me/12345");
    dummyData.setEncoding(List.of(videoEncoding));

    when(repository.findByIdentifier(dummyData.getIdentifier())).thenReturn(List.of(dummyData));

    OembedResponseDto oembed;
    oembed = service.getOembedResponse(dummyData.getIdentifier(), null, null);
    assertThat(oembed.getWidth()).isEqualTo(560);
    assertThat(oembed.getHeight()).isEqualTo(315);

    oembed = service.getOembedResponse(dummyData.getIdentifier(), 500, null);
    assertThat(oembed.getWidth()).isEqualTo(500);
    assertThat(oembed.getHeight()).isEqualTo(281);

    oembed = service.getOembedResponse(dummyData.getIdentifier(), 500, 250);
    assertThat(oembed.getWidth()).isEqualTo(444);
    assertThat(oembed.getHeight()).isEqualTo(250);

    oembed = service.getOembedResponse(dummyData.getIdentifier(), 400, 250);
    assertThat(oembed.getWidth()).isEqualTo(400);
    assertThat(oembed.getHeight()).isEqualTo(225);
  }

  @Test
  void testNotFound() {
    when(repository.findByIdentifier(Mockito.anyString())).thenReturn(List.of());
    OembedResponseDto oembed = service.getOembedResponse("https://some.id", null, null);
    assertThat(oembed).isNull();
  }

  @Test
  void testImageNotLoadable() {
    Metadata dummyData = newMetadata();
    dummyData.setImage("null");
    when(repository.findByIdentifier(dummyData.getIdentifier())).thenReturn(List.of(dummyData));
    OembedResponseDto oembed;
    oembed = service.getOembedResponse(dummyData.getIdentifier(), null, null);
    assertThat(oembed.getWidth()).isNull();
    assertThat(oembed.getHeight()).isNull();

    dummyData.setImage("ioexception");
    oembed = service.getOembedResponse(dummyData.getIdentifier(), null, null);
    assertThat(oembed.getThumbnailUrl()).isNull();
    assertThat(oembed.getThumbnailWidth()).isNull();
    assertThat(oembed.getThumbnailHeight()).isNull();
  }

  @Test
  void testFindByBase64EncodedUrl() {
    Metadata dummyData = newMetadata();
    when(repository.findByIdentifier("https://www.test.de")).thenReturn(List.of(dummyData));
    OembedResponseDto oembed = service.getOembedResponse("https://oersi.de/resources/aHR0cHM6Ly93d3cudGVzdC5kZQ==", null, null);
    assertThat(oembed).isNotNull();
  }

  @Test
  void testLicense() {
    Metadata dummyData = newMetadata();
    when(repository.findByIdentifier("https://www.test.de")).thenReturn(List.of(dummyData));
    OembedResponseDto oembed = service.getOembedResponse(dummyData.getIdentifier(), null, null);
    assertThat(oembed.getLicenseUrl()).isEqualTo(dummyData.getLicense().getIdentifier());

    dummyData.setLicense(null);
    oembed = service.getOembedResponse(dummyData.getIdentifier(), null, null);
    assertThat(oembed.getLicenseUrl()).isNull();
  }

  @Test
  void testAuthor() {
    Metadata dummyData = newMetadata();
    List<Creator> creators = new ArrayList<>();
    Creator author = new Creator();
    author.setType("Person");
    author.setName("test1 test");
    author.setIdentifier("https://orcid.org/1234-5678-0987-6543");
    Creator author2 = new Creator();
    author2.setType("Person");
    author2.setName("test2 test");
    author2.setIdentifier("https://orcid.org/5678-0987-6543-1234");
    creators.add(author);
    creators.add(author2);
    dummyData.setCreator(creators);

    when(repository.findByIdentifier("https://www.test.de")).thenReturn(List.of(dummyData));
    OembedResponseDto oembed = service.getOembedResponse(dummyData.getIdentifier(), null, null);
    assertThat(oembed.getAuthors()).hasSize(2);
    assertThat(oembed.getAuthorName()).isEqualTo("test1 test, test2 test");
    assertThat(oembed.getAuthorUrl()).isEqualTo("https://orcid.org/1234-5678-0987-6543,https://orcid.org/5678-0987-6543-1234");

    creators = new ArrayList<>();
    author = new Creator();
    author.setType("Person");
    author.setIdentifier("https://orcid.org/1234-5678-0987-6543");
    creators.add(author);
    dummyData.setCreator(creators);
    oembed = service.getOembedResponse(dummyData.getIdentifier(), null, null);
    assertThat(oembed.getAuthorName()).isNull();
  }

  @Test
  void testProvider() {
    Metadata dummyData = newMetadata();
    when(repository.findByIdentifier(dummyData.getIdentifier())).thenReturn(List.of(dummyData));
    OembedResponseDto oembed;
    oembed = service.getOembedResponse(dummyData.getIdentifier(), null, null);
    assertThat(oembed.getProviderName()).isNull();
    assertThat(oembed.getProviderUrl()).isNull();

    Provider provider = new Provider();
    provider.setName("YouTube");
    provider.setIdentifier("https://youtube.com/");
    dummyData.setProvider(provider);

    oembed = service.getOembedResponse(dummyData.getIdentifier(), null, null);
    assertThat(oembed.getProviderName()).isEqualTo("YouTube");
    assertThat(oembed.getProviderUrl()).isEqualTo("https://youtube.com/");
  }

  @Test
  void testThumbnail() {
    Metadata dummyData = newMetadata();
    when(repository.findByIdentifier(dummyData.getIdentifier())).thenReturn(List.of(dummyData));
    OembedResponseDto oembed;
    oembed = service.getOembedResponse(dummyData.getIdentifier(), null, null);
    assertThat(oembed.getThumbnailUrl()).isNull();
    assertThat(oembed.getThumbnailWidth()).isNull();
    assertThat(oembed.getThumbnailHeight()).isNull();

    dummyData.setImage("media/image001.png");
    oembed = service.getOembedResponse(dummyData.getIdentifier(), null, null);
    assertThat(oembed.getThumbnailUrl()).isNotNull();
    assertThat(oembed.getThumbnailWidth()).isEqualTo(900);
    assertThat(oembed.getThumbnailHeight()).isEqualTo(772);

    oembed = service.getOembedResponse(dummyData.getIdentifier(), 1000, 1000);
    assertThat(oembed.getThumbnailUrl()).isNotNull();
    assertThat(oembed.getThumbnailWidth()).isEqualTo(900);
    assertThat(oembed.getThumbnailHeight()).isEqualTo(772);

    oembed = service.getOembedResponse(dummyData.getIdentifier(), 800, null);
    assertThat(oembed.getThumbnailUrl()).isNull();
    assertThat(oembed.getThumbnailWidth()).isNull();
    assertThat(oembed.getThumbnailHeight()).isNull();

    oembed = service.getOembedResponse(dummyData.getIdentifier(), null, 750);
    assertThat(oembed.getThumbnailUrl()).isNull();
    assertThat(oembed.getThumbnailWidth()).isNull();
    assertThat(oembed.getThumbnailHeight()).isNull();
  }

}

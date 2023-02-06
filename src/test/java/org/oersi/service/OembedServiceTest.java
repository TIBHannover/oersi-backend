package org.oersi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.oersi.ElasticsearchServicesMock;
import org.oersi.domain.BackendMetadata;
import org.oersi.domain.OembedInfo;
import org.oersi.dto.OembedResponseDto;
import org.oersi.repository.MetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@Import(ElasticsearchServicesMock.class)
class OembedServiceTest {

  @Autowired
  private OembedService service;
  @Autowired
  private MetadataRepository repository; // mock from ElasticsearchServicesMock

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

  private BackendMetadata newMetadata() {
    BackendMetadata metadata = new BackendMetadata();
    OembedInfo oembedInfo = new OembedInfo();

    List<OembedInfo.Author> creators = new ArrayList<>();
    OembedInfo.Author author = new OembedInfo.Author();
    author.setName("test test");
    creators.add(author);
    oembedInfo.setAuthors(creators);

    oembedInfo.setLicenseUrl("https://creativecommons.org/licenses/by/4.0/");
    oembedInfo.setTitle("Test Title");
    metadata.setOembedInfo(oembedInfo);
    metadata.setId("aHR0cHM6Ly93d3cudGVzdC5kZQ=="); // https://www.test.de

    return metadata;
  }

  @Test
  void testVideoWidthAndHeight() {
    BackendMetadata dummyData = newMetadata();
    dummyData.getOembedInfo().setVideoEmbedUrl("https://embed.me/12345");

    when(repository.findById(dummyData.getId())).thenReturn(Optional.of(dummyData));

    OembedResponseDto oembed;
    oembed = service.getOembedResponse(dummyData.getId(), null, null);
    assertThat(oembed.getWidth()).isEqualTo(560);
    assertThat(oembed.getHeight()).isEqualTo(315);

    oembed = service.getOembedResponse(dummyData.getId(), 500, null);
    assertThat(oembed.getWidth()).isEqualTo(500);
    assertThat(oembed.getHeight()).isEqualTo(281);

    oembed = service.getOembedResponse(dummyData.getId(), 500, 250);
    assertThat(oembed.getWidth()).isEqualTo(444);
    assertThat(oembed.getHeight()).isEqualTo(250);

    oembed = service.getOembedResponse(dummyData.getId(), 400, 250);
    assertThat(oembed.getWidth()).isEqualTo(400);
    assertThat(oembed.getHeight()).isEqualTo(225);
  }

  @Test
  void testNotFound() {
    when(repository.findById(Mockito.anyString())).thenReturn(Optional.empty());
    OembedResponseDto oembed = service.getOembedResponse("https://some.id", null, null);
    assertThat(oembed).isNull();
  }

  @Test
  void testImageNotLoadable() {
    BackendMetadata dummyData = newMetadata();
    dummyData.getOembedInfo().setThumbnailUrl("null");
    when(repository.findById(dummyData.getId())).thenReturn(Optional.of(dummyData));
    OembedResponseDto oembed;
    oembed = service.getOembedResponse(dummyData.getId(), null, null);
    assertThat(oembed.getWidth()).isNull();
    assertThat(oembed.getHeight()).isNull();

    dummyData.getOembedInfo().setThumbnailUrl("ioexception");
    oembed = service.getOembedResponse(dummyData.getId(), null, null);
    assertThat(oembed.getThumbnailUrl()).isNull();
    assertThat(oembed.getThumbnailWidth()).isNull();
    assertThat(oembed.getThumbnailHeight()).isNull();
  }

  @Test
  void testFindByBase64EncodedUrl() {
    BackendMetadata dummyData = newMetadata();
    when(repository.findById(dummyData.getId())).thenReturn(Optional.of(dummyData));
    OembedResponseDto oembed = service.getOembedResponse("https://oersi.de/resources/aHR0cHM6Ly93d3cudGVzdC5kZQ==", null, null);
    assertThat(oembed).isNotNull();
  }

  @Test
  void testLicense() {
    BackendMetadata dummyData = newMetadata();
    when(repository.findById(dummyData.getId())).thenReturn(Optional.of(dummyData));
    OembedResponseDto oembed = service.getOembedResponse(dummyData.getId(), null, null);
    assertThat(oembed.getLicenseUrl()).isEqualTo(dummyData.getOembedInfo().getLicenseUrl());

    dummyData.getOembedInfo().setLicenseUrl(null);
    oembed = service.getOembedResponse(dummyData.getId(), null, null);
    assertThat(oembed.getLicenseUrl()).isNull();
  }

  @Test
  void testAuthor() {
    BackendMetadata dummyData = newMetadata();
    List<OembedInfo.Author> creators = new ArrayList<>();
    OembedInfo.Author author = new OembedInfo.Author();
    author.setName("test1 test");
    author.setUrl("https://orcid.org/1234-5678-0987-6543");
    OembedInfo.Author author2 = new OembedInfo.Author();
    author2.setName("test2 test");
    author2.setUrl("https://orcid.org/5678-0987-6543-1234");
    creators.add(author);
    creators.add(author2);
    dummyData.getOembedInfo().setAuthors(creators);

    when(repository.findById(dummyData.getId())).thenReturn(Optional.of(dummyData));
    OembedResponseDto oembed = service.getOembedResponse(dummyData.getId(), null, null);
    assertThat(oembed.getAuthors()).hasSize(2);
    assertThat(oembed.getAuthorName()).isEqualTo("test1 test, test2 test");
    assertThat(oembed.getAuthorUrl()).isEqualTo("https://orcid.org/1234-5678-0987-6543,https://orcid.org/5678-0987-6543-1234");

    creators = new ArrayList<>();
    author = new OembedInfo.Author();
    author.setUrl("https://orcid.org/1234-5678-0987-6543");
    creators.add(author);
    dummyData.getOembedInfo().setAuthors(creators);
    oembed = service.getOembedResponse(dummyData.getId(), null, null);
    assertThat(oembed.getAuthorName()).isNull();
  }

  @Test
  void testProvider() {
    BackendMetadata dummyData = newMetadata();
    when(repository.findById(dummyData.getId())).thenReturn(Optional.of(dummyData));
    OembedResponseDto oembed;
    oembed = service.getOembedResponse(dummyData.getId(), null, null);
    assertThat(oembed.getProviderName()).isNull();
    assertThat(oembed.getProviderUrl()).isNull();

    dummyData.getOembedInfo().setProviderName("YouTube");
    dummyData.getOembedInfo().setProviderUrl("https://youtube.com/");

    oembed = service.getOembedResponse(dummyData.getId(), null, null);
    assertThat(oembed.getProviderName()).isEqualTo("YouTube");
    assertThat(oembed.getProviderUrl()).isEqualTo("https://youtube.com/");
  }

  @Test
  void testThumbnail() {
    BackendMetadata dummyData = newMetadata();
    when(repository.findById(dummyData.getId())).thenReturn(Optional.of(dummyData));
    OembedResponseDto oembed;
    oembed = service.getOembedResponse(dummyData.getId(), null, null);
    assertThat(oembed.getThumbnailUrl()).isNull();
    assertThat(oembed.getThumbnailWidth()).isNull();
    assertThat(oembed.getThumbnailHeight()).isNull();

    dummyData.getOembedInfo().setThumbnailUrl("media/image001.png");
    oembed = service.getOembedResponse(dummyData.getId(), null, null);
    assertThat(oembed.getThumbnailUrl()).isNotNull();
    assertThat(oembed.getThumbnailWidth()).isEqualTo(900);
    assertThat(oembed.getThumbnailHeight()).isEqualTo(772);

    oembed = service.getOembedResponse(dummyData.getId(), 1000, 1000);
    assertThat(oembed.getThumbnailUrl()).isNotNull();
    assertThat(oembed.getThumbnailWidth()).isEqualTo(900);
    assertThat(oembed.getThumbnailHeight()).isEqualTo(772);

    oembed = service.getOembedResponse(dummyData.getId(), 800, null);
    assertThat(oembed.getThumbnailUrl()).isNull();
    assertThat(oembed.getThumbnailWidth()).isNull();
    assertThat(oembed.getThumbnailHeight()).isNull();

    oembed = service.getOembedResponse(dummyData.getId(), null, 750);
    assertThat(oembed.getThumbnailUrl()).isNull();
    assertThat(oembed.getThumbnailWidth()).isNull();
    assertThat(oembed.getThumbnailHeight()).isNull();
  }

}

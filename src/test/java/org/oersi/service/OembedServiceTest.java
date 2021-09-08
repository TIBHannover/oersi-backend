package org.oersi.service;

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

  private Metadata newMetadata() {
    Metadata metadata = new Metadata();

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
  void testFindByBase64EncodedUrl() {
    Metadata dummyData = newMetadata();
    when(repository.findByIdentifier("https://www.test.de")).thenReturn(List.of(dummyData));
    OembedResponseDto oembed = service.getOembedResponse("https://oersi.de/resources/aHR0cHM6Ly93d3cudGVzdC5kZQ==", null, null);
    assertThat(oembed).isNotNull();
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
  void testNullThumbnail() {
    Metadata dummyData = newMetadata();
    when(repository.findByIdentifier(dummyData.getIdentifier())).thenReturn(List.of(dummyData));

    OembedResponseDto oembed;
    oembed = service.getOembedResponse(dummyData.getIdentifier(), null, null);
    assertThat(oembed.getThumbnailUrl()).isNull();
    assertThat(oembed.getThumbnailWidth()).isNull();
    assertThat(oembed.getThumbnailHeight()).isNull();

    dummyData.setImage("https://awesome.image");
    oembed = service.getOembedResponse(dummyData.getIdentifier(), null, null);
    assertThat(oembed.getThumbnailUrl()).isNull();
    assertThat(oembed.getThumbnailWidth()).isNull();
    assertThat(oembed.getThumbnailHeight()).isNull();

    dummyData.setImageWidth(400);
    oembed = service.getOembedResponse(dummyData.getIdentifier(), null, null);
    assertThat(oembed.getThumbnailUrl()).isNull();
    assertThat(oembed.getThumbnailWidth()).isNull();
    assertThat(oembed.getThumbnailHeight()).isNull();

    dummyData.setImageWidth(null);
    dummyData.setImageHeight(250);
    oembed = service.getOembedResponse(dummyData.getIdentifier(), null, null);
    assertThat(oembed.getThumbnailUrl()).isNull();
    assertThat(oembed.getThumbnailWidth()).isNull();
    assertThat(oembed.getThumbnailHeight()).isNull();
  }

  @Test
  void testThumbnail() {
    Metadata dummyData = newMetadata();
    dummyData.setImage("https://awesome.image");
    dummyData.setImageWidth(400);
    dummyData.setImageHeight(250);
    when(repository.findByIdentifier(dummyData.getIdentifier())).thenReturn(List.of(dummyData));

    OembedResponseDto oembed;
    oembed = service.getOembedResponse(dummyData.getIdentifier(), 300, 300);
    assertThat(oembed.getThumbnailUrl()).isNull();
    assertThat(oembed.getThumbnailWidth()).isNull();
    assertThat(oembed.getThumbnailHeight()).isNull();

    oembed = service.getOembedResponse(dummyData.getIdentifier(), 300, null);
    assertThat(oembed.getThumbnailUrl()).isNull();
    assertThat(oembed.getThumbnailWidth()).isNull();
    assertThat(oembed.getThumbnailHeight()).isNull();

    oembed = service.getOembedResponse(dummyData.getIdentifier(), 600, 200);
    assertThat(oembed.getThumbnailUrl()).isNull();
    assertThat(oembed.getThumbnailWidth()).isNull();
    assertThat(oembed.getThumbnailHeight()).isNull();

    oembed = service.getOembedResponse(dummyData.getIdentifier(), null, 200);
    assertThat(oembed.getThumbnailUrl()).isNull();
    assertThat(oembed.getThumbnailWidth()).isNull();
    assertThat(oembed.getThumbnailHeight()).isNull();

    oembed = service.getOembedResponse(dummyData.getIdentifier(), 520, 315);
    assertThat(oembed.getThumbnailUrl()).isEqualTo(dummyData.getImage());
    assertThat(oembed.getThumbnailWidth()).isEqualTo(400);
    assertThat(oembed.getThumbnailHeight()).isEqualTo(250);

    oembed = service.getOembedResponse(dummyData.getIdentifier(), null, null);
    assertThat(oembed.getThumbnailUrl()).isEqualTo(dummyData.getImage());
    assertThat(oembed.getThumbnailWidth()).isEqualTo(400);
    assertThat(oembed.getThumbnailHeight()).isEqualTo(250);
  }

}

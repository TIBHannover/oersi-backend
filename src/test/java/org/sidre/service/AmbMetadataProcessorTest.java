package org.sidre.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.sidre.ElasticsearchServicesMock;
import org.sidre.connector.RorConnector;
import org.sidre.domain.BackendConfig;
import org.sidre.domain.BackendMetadata;
import org.sidre.domain.OembedInfo;
import org.sidre.domain.OrganizationInfo;
import org.sidre.repository.BackendConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@ElasticsearchServicesMock
class AmbMetadataProcessorTest {

  @Autowired
  private AmbMetadataProcessor processor;
  @Autowired
  private ConfigService configService;

  @MockitoBean
  private RorConnector rorConnector;
  @MockitoBean
  private VocabService vocabService;
  @Autowired
  private BackendConfigRepository configRepository; // mock from ElasticsearchServicesMock

  @BeforeEach
  void setup() {
    configService.updateMetadataConfig(null);
  }

  @Test
  void testAboutWithMultipleParentSubjects() {
    BackendMetadata data = MetadataFieldServiceImpl.toMetadata(
            new HashMap<>(Map.of(
                    "id", "https://www.test.de",
                    "about", List.of(
                            Map.of("id", "https://w3id.org/kim/hochschulfaechersystematik/n4"),
                            Map.of("id", "https://w3id.org/kim/hochschulfaechersystematik/n6"),
                            Map.of("id", "https://w3id.org/kim/hochschulfaechersystematik/n9")
                    )
            )
            ), "id");
    when(vocabService.getParentMap("hochschulfaechersystematik")).thenReturn(Map.of(
            "https://w3id.org/kim/hochschulfaechersystematik/n42", "https://w3id.org/kim/hochschulfaechersystematik/n4"
    ));

    processor.process(data);

    assertThat(data.get("about")).isInstanceOf(List.class);
    assertThat((List<?>) data.get("about")).hasSize(1);
  }

  @Test
  void testOembedInfo() {
    BackendMetadata data = MetadataFieldServiceImpl.toMetadata(
      Map.of(
        "id", "https://www.test.de",
        "name", "test",
        "creator", List.of(
          Map.of(
            "type", "Person",
            "name", "GivenName FamilyName"
          ),
          Map.of(
            "type", "Organization",
            "name", "name",
            "id", "https://example.org/ror"
          )
        ),
        "image", "https://example.org/image/123.png",
        "encoding", List.of(new HashMap<>(Map.of(
          "type", "MediaObject",
          "embedUrl", "https://example.org/embed/#/123"
        )))
      ), "id");
    OembedInfo oembedInfo = new OembedInfo();
    processor.processOembedInfo(oembedInfo, data);
    assertThat(oembedInfo.getTitle()).isEqualTo(data.getData().get("name"));
    assertThat(oembedInfo.getAuthors()).hasSize(2);
    assertThat(oembedInfo.getThumbnailUrl()).isEqualTo(data.getData().get("image"));
    assertThat(oembedInfo.getVideoEmbedUrl()).isEqualTo("https://example.org/embed/#/123");
  }

  @Test
  void testCreatorToPersonsMapping() {
    BackendMetadata data = MetadataFieldServiceImpl.toMetadata(
      new HashMap<>(Map.of(
        "id", "https://www.test.de",
        "name", "test",
        "creator", List.of(
          Map.of(
            "type", "Person",
            "name", "GivenName FamilyName"
          ),
          Map.of(
            "type", "Organization",
            "name", "name",
            "id", "https://example.org/ror"
          )
        )
      )), "id");
    processor.process(data);
    processor.postProcess(data);
    assertThat(data.getExtendedData()).isNotNull().containsEntry("persons", List.of(Map.of("type", "Person", "name", "GivenName FamilyName")));
  }
  @Test
  void testPublisherToInstitutionWhitelistMapping() {
    BackendConfig config = new BackendConfig();
    config.setCustomConfig(Map.of(
      "institutionMapping", List.of(
              Map.of("regex", "(.*not whitelisted.*)", "copyFromPublisher", "false"),
              Map.of("regex", ".*(ABC-institution).*", "copyFromPublisher", "true"),
              Map.of("regex", ".*(XxxYyyZzz-institute of technology).*", "internalName", "XYZ-institute", "copyFromPublisher", "true")
      )
    ));
    when(configRepository.findById("search_index_backend_config")).thenReturn(Optional.of(config));
    BackendMetadata data = MetadataFieldServiceImpl.toMetadata(
      new HashMap<>(Map.of(
        "id", "https://www.test.de",
        "name", "test",
        "publisher", List.of(
          Map.of(
            "type", "Organization",
            "name", "an organization that is not whitelisted"
          ),
          Map.of(
            "type", "Organization",
            "name", "an amazing ABC-institution (great)"
          ),
          Map.of(
            "type", "Organization",
            "name", "XxxYyyZzz-institute of technology"
          )
        )
      )), "id");
    processor.process(data);
    processor.postProcess(data);
    assertThat(
      data.getExtendedData()).isNotNull()
      .containsEntry(
        "institutions", List.of(
          Map.of("type", "Organization", "name", "ABC-institution"),
          Map.of("type", "Organization", "name", "XYZ-institute")
      )
    );
  }

  @Test
  void testInstitutionDefaultIdMapping() {
    BackendConfig config = new BackendConfig();
    config.setCustomConfig(Map.of(
            "institutionMapping", List.of(
                    Map.of("regex", ".*(ABC-institution).*", "copyFromPublisher", "true", "defaultId", "https://ror.org/id1"),
                    Map.of("regex", ".*(XYZ-institute).*", "internalName", "XYZ-institute", "copyFromPublisher", "true", "defaultId", "https://ror.org/id2")
            )
    ));
    when(configRepository.findById("search_index_backend_config")).thenReturn(Optional.of(config));
    BackendMetadata data = MetadataFieldServiceImpl.toMetadata(
            new HashMap<>(Map.of(
                    "id", "https://www.test.de",
                    "name", "test",
                    "creator", List.of(
                            Map.of(
                                    "type", "Organization",
                                    "name", "an organization without id"
                            ),
                            Map.of(
                                    "type", "Organization",
                                    "name", "ABC-institution"
                            ),
                            Map.of(
                                    "type", "Person",
                                    "name", "Mustermensch",
                                    "affiliation", Map.of(
                                            "type", "Organization",
                                            "name", "ABC-institution"
                                    )
                            ),
                            Map.of(
                                    "type", "Organization",
                                    "name", "XYZ-institute",
                                    "id", "https://example.org/someid"
                            )
                    )
            )), "id");
    processor.process(data);
    assertThat(
            data.getData()).isNotNull()
            .containsEntry(
                    "creator", List.of(
                            Map.of("type", "Organization", "name", "an organization without id"),
                            Map.of("type", "Organization", "name", "ABC-institution", "id", "https://ror.org/id1"),
                            Map.of(
                                    "type", "Person",
                                    "name", "Mustermensch",
                                    "affiliation", Map.of(
                                            "type", "Organization",
                                            "name", "ABC-institution",
                                            "id", "https://ror.org/id1"
                                    )
                            ),
                            Map.of("type", "Organization", "name", "XYZ-institute", "id", "https://example.org/someid")
                    )
            );
  }

  @Test
  void testRorLocationData() {
    OrganizationInfo resp = new OrganizationInfo();
    resp.setOrganizationId("https://ror.org/04aj4c181");
    var location = new OrganizationInfo.Location();
    var address = new OrganizationInfo.Location.Address();
    address.setAddressCountry("DE");
    address.setAddressRegion("Lower Saxony");
    location.setAddress(address);
    resp.setLocations(List.of(location));
    when(rorConnector.loadOrganizationInfo(Mockito.anyString())).thenReturn(null);
    when(rorConnector.loadOrganizationInfo("https://ror.org/04aj4c181")).thenReturn(resp);
    BackendMetadata data = MetadataFieldServiceImpl.toMetadata(
            new HashMap<>(Map.of(
                    "id", "https://www.test.de",
                    "name", "test",
                    "sourceOrganization", List.of(
                            Map.of(
                                    "type", "Organization",
                                    "name", "an organization without id"
                            ),
                            Map.of(
                                    "type", "Organization",
                                    "name", "an organization without known id",
                                    "id", "https://example.org/id"
                            ),
                            Map.of(
                                    "type", "Organization",
                                    "name", "organization with ror id",
                                    "id", "https://ror.org/04aj4c181"
                            )
                    )
            )), "id");
    processor.setFeatureAddExternalOrganizationInfo(true);
    processor.process(data);
    processor.postProcess(data);
    Map<String, Object> expectedLocation = MetadataHelper.format(location);
    assertThat(
            data.getExtendedData()).isNotNull()
            .containsEntry(
                    "institutions", List.of(
                            Map.of("type", "Organization", "name", "an organization without id"),
                            Map.of("id", "https://example.org/id","type", "Organization", "name", "an organization without known id"),
                            Map.of("id", "https://ror.org/04aj4c181",
                                    "type", "Organization",
                                    "name", "organization with ror id",
                                    "location", List.of(expectedLocation)
                            )
                    )
            );
  }

  @Test
  void testEncodingDefaultValueForType() {
    BackendMetadata data = MetadataFieldServiceImpl.toMetadata(
      new HashMap<>(Map.of(
        "id", "https://www.test.de",
        "name", "test",
        "encoding", List.of(
          Map.of(
            "contentUrl", "https://example.org/contentUrl"
          ),
          Map.of(
            "embedUrl", "https://example.org/embed/123"
          )
        )
      )), "id");
    processor.process(data);
    assertThat(data.getData()).isNotNull()
      .containsEntry("encoding", List.of(
        Map.of("contentUrl", "https://example.org/contentUrl", "type", "MediaObject"),
        Map.of("embedUrl", "https://example.org/embed/123", "type", "MediaObject")
      ));
  }

}

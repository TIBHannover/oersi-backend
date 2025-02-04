package org.sidre.service;

import com.fasterxml.jackson.core.type.TypeReference;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sidre.ElasticsearchServicesMock;
import org.sidre.domain.BackendMetadata;
import org.sidre.domain.OrganizationInfo;
import org.sidre.repository.OrganizationInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class MetadataServiceWithCustomConfigTest {

  @Nested
  @SpringBootTest(properties = {"feature.amb.add_external_organization_info=true"})
  @Import(ElasticsearchServicesMock.class)
  class MetadataServiceAddLocationTest {

    @Autowired
    private MetadataService service;
    @Autowired
    private OrganizationInfoRepository organizationInfoRepository; // mock from ElasticsearchServicesMock

    private static @NotNull OrganizationInfo newExampleOrganizationInfo(String organizationId) {
      OrganizationInfo organizationInfo = new OrganizationInfo();
      organizationInfo.setDateUpdated(LocalDateTime.now());
      organizationInfo.setOrganizationId(organizationId);
      OrganizationInfo.Location location = new OrganizationInfo.Location();
      location.setGeo(new GeoPoint(52.37052, 9.73322));
      OrganizationInfo.Location.Address address = new OrganizationInfo.Location.Address();
      address.setAddressCountry("DE");
      address.setAddressRegion("Lower Saxony");
      address.setAddressLocality("Hanover");
      location.setAddress(address);
      organizationInfo.setLocations(List.of(location));
      return organizationInfo;
    }

    private static @NotNull Map<String, Object> newExampleOrganization() {
      return Map.of(
          "type", "Organization",
          "name", "name",
          "id", "https://example.org/ror"
      );
    }


    private BackendMetadata newMetadata() {
      return MetadataFieldServiceImpl.toMetadata(new HashMap<>(Map.ofEntries(
          Map.entry("@context", List.of("https://w3id.org/kim/amb/context.jsonld", Map.of("@language", "de"))),
          Map.entry("id", "https://www.test.de"),
          Map.entry("name", "Test Title"),
          Map.entry("mainEntityOfPage", new ArrayList<>(List.of(
              Map.of(
                  "id", "http://example.url/desc/123",
                  "provider", Map.of("id", "http://example.url/provider/testprovider", "name", "provider name")
              )
          )))
      )), "id");
    }

    @Test
    void testAddLocationToSourceOrganization() {
      BackendMetadata metadata = newMetadata();
      Map<String, Object> cachedOrganization = newExampleOrganization();
      metadata.getData().put("sourceOrganization", new ArrayList<>(List.of(cachedOrganization)));
      OrganizationInfo organizationInfo = newExampleOrganizationInfo((String) cachedOrganization.get("id"));
      when(organizationInfoRepository.findById((String) cachedOrganization.get("id"))).thenReturn(Optional.of(organizationInfo));

      BackendMetadata result = service.createOrUpdate(metadata).getMetadata();
      List<Map<String, Object>> organizations = MetadataHelper.parseList(result.getData(), "sourceOrganization", new TypeReference<>() {
      });
      assertNotNull(organizations);
      assertEquals(1, organizations.size());
      assertContainsExpectedLocation(organizations.get(0));
    }

    @Test
    void testAddLocationToPublisher() {
      BackendMetadata metadata = newMetadata();
      Map<String, Object> cachedOrganization = newExampleOrganization();
      metadata.getData().put("publisher", new ArrayList<>(List.of(cachedOrganization)));
      OrganizationInfo organizationInfo = newExampleOrganizationInfo((String) cachedOrganization.get("id"));
      when(organizationInfoRepository.findById((String) cachedOrganization.get("id"))).thenReturn(Optional.of(organizationInfo));

      BackendMetadata result = service.createOrUpdate(metadata).getMetadata();
      List<Map<String, Object>> organizations = MetadataHelper.parseList(result.getData(), "publisher", new TypeReference<>() {
      });
      assertNotNull(organizations);
      assertEquals(1, organizations.size());
      assertContainsExpectedLocation(organizations.get(0));
    }

    @Test
    void testAddLocationToCreatorOrganization() {
      BackendMetadata metadata = newMetadata();
      Map<String, Object> cachedOrganization = newExampleOrganization();
      metadata.getData().put("creator", new ArrayList<>(List.of(
          Map.of(
              "type", "Person",
              "name", "test test"
          ),
          cachedOrganization
      )));
      OrganizationInfo organizationInfo = newExampleOrganizationInfo((String) cachedOrganization.get("id"));
      when(organizationInfoRepository.findById((String) cachedOrganization.get("id"))).thenReturn(Optional.of(organizationInfo));

      BackendMetadata result = service.createOrUpdate(metadata).getMetadata();
      List<Map<String, Object>> creators = MetadataHelper.parseList(result.getData(), "creator", new TypeReference<>() {
      });
      assertNotNull(creators);
      List<Map<String, Object>> organizations = creators.stream().filter(c -> "Organization".equals(c.get("type"))).toList();
      assertEquals(1, organizations.size());
      assertContainsExpectedLocation(organizations.get(0));
    }

    @Test
    void testAddLocationToCreatorAffiliation() {
      BackendMetadata metadata = newMetadata();
      Map<String, Object> cachedOrganization = newExampleOrganization();
      metadata.getData().put("creator", new ArrayList<>(List.of(
          Map.of(
              "type", "Person",
              "name", "test test",
              "affiliation", cachedOrganization
          )
      )));
      OrganizationInfo organizationInfo = newExampleOrganizationInfo((String) cachedOrganization.get("id"));
      when(organizationInfoRepository.findById((String) cachedOrganization.get("id"))).thenReturn(Optional.of(organizationInfo));

      BackendMetadata result = service.createOrUpdate(metadata).getMetadata();
      List<Map<String, Object>> creators = MetadataHelper.parseList(result.getData(), "creator", new TypeReference<>() {
      });
      assertNotNull(creators);
      assertEquals(1, creators.size());
      Map<String, Object> affiliation = MetadataHelper.parse(creators.get(0), "affiliation", new TypeReference<>() {
      });
      assertNotNull(affiliation);
      assertContainsExpectedLocation(affiliation);
    }

    private static void assertContainsExpectedLocation(Map<String, Object> organization) {
      Map<String, Object> expectedLocation = Map.of(
          "address", Map.of(
              "addressCountry", "DE",
              "addressRegion", "Lower Saxony",
              "addressLocality", "Hanover"
          )
      );
      assertEquals(List.of(expectedLocation), organization.get("location"));
    }

  }

}

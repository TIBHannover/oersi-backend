package org.sidre.connector;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sidre.domain.OrganizationInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Qualifier("ror")
public class RorConnector implements OrganizationInfoConnector {

  private final WebClient webClient;

  @Override
  public OrganizationInfo loadOrganizationInfo(String rorId) {
    if (rorId.startsWith("https://ror.org/")) {
      var id = rorId.replace("https://ror.org/", "");
      Mono<OrganizationInfo> rorResponse = webClient.get()
              .uri("https://api.ror.org/organizations/" + id)
              .retrieve()
              .onStatus(HttpStatusCode::isError, clientResponse -> Mono.error(new IOException("No successful result from ror API " + clientResponse.statusCode().value())))
              .bodyToMono(RorOrganization.class)
              .flatMap(rorOrganization -> Mono.justOrEmpty(toOrganizationInfo(rorOrganization)))
              .onErrorResume(IOException.class, e -> {
                log.warn("Cannot load organization info for '" + rorId + "' from ror API: " + e.getMessage());
                return Mono.empty();
              });
      return rorResponse.block();
    }
    log.debug("Is not a valid ror Id: " + rorId);
    return null;
  }

  private OrganizationInfo toOrganizationInfo(RorOrganization rorOrganization) {
    if (rorOrganization == null || rorOrganization.id == null || rorOrganization.locations == null) {
      return null;
    }
    OrganizationInfo info = new OrganizationInfo();
    info.setOrganizationId(rorOrganization.id);
    List<OrganizationInfo.Location> locations = new ArrayList<>();
    for (RorOrganization.Location.GeonamesDetails geonamesDetails : rorOrganization.locations.stream().map(loc -> loc.geonamesDetails).filter(Objects::nonNull).toList()) {
      OrganizationInfo.Location location = new OrganizationInfo.Location();
      if (geonamesDetails.lat != null && geonamesDetails.lng != null) {
        location.setGeo(new GeoPoint(geonamesDetails.lat, geonamesDetails.lng));
      }
      OrganizationInfo.Location.Address locationAddress = new OrganizationInfo.Location.Address();
      locationAddress.setAddressCountry(geonamesDetails.country);
      locationAddress.setAddressLocality(geonamesDetails.name);
      locationAddress.setAddressRegion(geonamesDetails.region);
      location.setAddress(locationAddress);
      locations.add(location);
    }
    info.setLocations(locations);
    return info;
  }

  @Data
  protected static class RorOrganization {
    private String id;
    private List<Location> locations;

    @Data
    protected static class Location {
      @JsonProperty("geonames_details")
      private GeonamesDetails geonamesDetails;

      @Data
      protected static class GeonamesDetails {
        private Double lat;
        private Double lng;
        private String name;
        @JsonProperty("country_name")
        private String country;
        @JsonProperty("country_subdivision_name")
        private String region;
      }
    }
  }

}

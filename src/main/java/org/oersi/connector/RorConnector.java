package org.oersi.connector;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oersi.domain.OrganizationInfo;
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
              .map(this::toOrganizationInfo)
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
    OrganizationInfo info = new OrganizationInfo();
    info.setOrganizationId(rorOrganization.id);
    List<OrganizationInfo.Location> locations = new ArrayList<>();
    for (RorOrganization.Address address : rorOrganization.addresses) {
      OrganizationInfo.Location location = new OrganizationInfo.Location();
      location.setGeo(new GeoPoint(address.lat, address.lng));
      OrganizationInfo.Location.Address locationAddress = new OrganizationInfo.Location.Address();
      locationAddress.setAddressCountry(rorOrganization.country.countryCode);
      locationAddress.setAddressLocality(address.city);
      locationAddress.setAddressRegion(address.geonamesCity.geonamesAdmin1.name);
      location.setAddress(locationAddress);
      locations.add(location);
    }
    info.setLocations(locations);
    return info;
  }

  @Data
  protected static class RorOrganization {
    private String id;
    private String name;
    private Country country;
    private List<Address> addresses;

    @Data
    protected static class Country {
      @JsonProperty("country_code")
      private String countryCode;
    }

    @Data
    protected static class Address {
      private Double lat;
      private Double lng;
      private String city;
      @JsonProperty("geonames_city")
      private GeonamesCity geonamesCity;

      @Data
      protected static class GeonamesCity {
        @JsonProperty("geonames_admin1")
        private GeonamesAdmin1 geonamesAdmin1;

        @Data
        protected static class GeonamesAdmin1 {
          private String name;
        }
      }
    }
  }

}

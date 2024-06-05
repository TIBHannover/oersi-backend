package org.sidre.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(indexName = "search_index_backend_organization_info", dynamic = Dynamic.FALSE)
public class OrganizationInfo {

  @Id
  private String organizationId;
  private List<Location> locations;

  @Field(type= FieldType.Date, format = DateFormat.date_hour_minute_second)
  private LocalDateTime dateUpdated;

  @Data
  public static class Location {
    private GeoPoint geo;
    private Address address;

    @Data
    public static class Address {
      private String addressCountry;
      private String addressLocality;
      private String addressRegion;
    }
  }


}

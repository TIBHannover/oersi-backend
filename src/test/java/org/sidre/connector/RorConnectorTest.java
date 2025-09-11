package org.sidre.connector;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.sidre.ElasticsearchServicesMock;
import org.sidre.domain.OrganizationInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@ElasticsearchServicesMock
class RorConnectorTest {

  @Autowired
  private RorConnector rorConnector;

  @MockitoBean
  private WebClient webClient;

  private void mockResponse(RorConnector.RorOrganization resp) {
    final var uriSpecMock = Mockito.mock(WebClient.RequestHeadersUriSpec.class);
    final var headersSpecMock = Mockito.mock(WebClient.RequestHeadersSpec.class);
    final var responseSpecMock = Mockito.mock(WebClient.ResponseSpec.class);
    final var onStatusSpecMock = Mockito.mock(WebClient.ResponseSpec.class);
    when(webClient.get()).thenReturn(uriSpecMock);
    when(uriSpecMock.uri(ArgumentMatchers.<String>notNull())).thenReturn(headersSpecMock);
    when(headersSpecMock.retrieve()).thenReturn(responseSpecMock);
    when(responseSpecMock.onStatus(Mockito.any(), Mockito.any())).thenReturn(onStatusSpecMock);
    when(onStatusSpecMock.bodyToMono(RorConnector.RorOrganization.class))
            .thenReturn(Mono.justOrEmpty(resp));
  }

  private RorConnector.RorOrganization getTestData() {
    var testData = new RorConnector.RorOrganization();
    testData.setId("https://ror.org/04aj4c181");
    List<RorConnector.RorOrganization.Location> locations = new ArrayList<>();
    var address = new RorConnector.RorOrganization.Location.GeonamesDetails();
    address.setName("Hanover");
    address.setCountry("DE");
    address.setLat(52.37052);
    address.setLng(9.73322);
    address.setRegion("Lower Saxony");
    RorConnector.RorOrganization.Location location = new RorConnector.RorOrganization.Location();
    location.setGeonamesDetails(address);
    locations.add(location);
    testData.setLocations(locations);
    return testData;
  }

  @Test
  void testLoadOrganizationInfo() {
    mockResponse(getTestData());

    OrganizationInfo info = rorConnector.loadOrganizationInfo("https://ror.org/04aj4c181");
    assertThat(info).isNotNull();
    assertThat(info.getLocations()).hasSize(1);
  }

  @Test
  void testLoadOrganizationInfoWithMissingData() {
    mockResponse(null);
    OrganizationInfo info = rorConnector.loadOrganizationInfo("https://ror.org/04aj4c181");
    assertThat(info).isNull();
  }


  @Test
  void testLoadOrganizationInfoWithMissingId() {
    var testData = getTestData();
    testData.setId(null);
    mockResponse(testData);
    OrganizationInfo info = rorConnector.loadOrganizationInfo("https://ror.org/04aj4c181");
    assertThat(info).isNull();
  }

  @Test
  void testLoadOrganizationInfoWithoutLatLon() {
    RorConnector.RorOrganization testData = getTestData();
    testData.getLocations().get(0).getGeonamesDetails().setLat(null);
    testData.getLocations().get(0).getGeonamesDetails().setLng(null);
    mockResponse(testData);

    OrganizationInfo info = rorConnector.loadOrganizationInfo("https://ror.org/04aj4c181");
    assertThat(info).isNotNull();
    assertThat(info.getLocations()).hasSize(1);
  }

  @Test
  void testInvalidUrl() {
    var result = rorConnector.loadOrganizationInfo("https://example.org/something");
    assertThat(result).isNull();
  }
}

package org.sidre.connector;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.sidre.ElasticsearchServicesMock;
import org.sidre.domain.OrganizationInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@Import(ElasticsearchServicesMock.class)
class RorConnectorTest {

  @Autowired
  private RorConnector rorConnector;

  @MockBean
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
    when(onStatusSpecMock.bodyToMono(ArgumentMatchers.<Class<RorConnector.RorOrganization>>notNull()))
            .thenReturn(Mono.just(resp));
  }

  private RorConnector.RorOrganization getTestData() {
    var testData = new RorConnector.RorOrganization();
    testData.setId("https://ror.org/04aj4c181");
    var country = new RorConnector.RorOrganization.Country();
    country.setCountryCode("DE");
    testData.setCountry(country);
    List<RorConnector.RorOrganization.Address> addresses = new ArrayList<>();
    var address = new RorConnector.RorOrganization.Address();
    address.setCity("Hanover");
    address.setLat(52.37052);
    address.setLng(9.73322);
    var geonamesCity = new RorConnector.RorOrganization.Address.GeonamesCity();
    var geonamesAdmin1 = new RorConnector.RorOrganization.Address.GeonamesCity.GeonamesAdmin1();
    geonamesAdmin1.setName("Lower Saxony");
    geonamesCity.setGeonamesAdmin1(geonamesAdmin1);
    address.setGeonamesCity(geonamesCity);
    addresses.add(address);
    testData.setAddresses(addresses);
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
  void testLoadOrganizationInfoWithoutLatLon() {
    RorConnector.RorOrganization testData = getTestData();
    testData.getAddresses().get(0).setLat(null);
    testData.getAddresses().get(0).setLng(null);
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

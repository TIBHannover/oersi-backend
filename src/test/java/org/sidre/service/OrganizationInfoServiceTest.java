package org.sidre.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.sidre.ElasticsearchServicesMock;
import org.sidre.connector.RorConnector;
import org.sidre.domain.OrganizationInfo;
import org.sidre.repository.OrganizationInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@Import(ElasticsearchServicesMock.class)
class OrganizationInfoServiceTest {

  @MockBean
  private RorConnector rorConnector;
  @Autowired
  private OrganizationInfoRepository organizationInfoRepository; // mock from ElasticsearchServicesMock
  @Autowired
  private OrganizationInfoService organizationInfoService;

  private OrganizationInfo getTestData() {
    OrganizationInfo resp = new OrganizationInfo();
    resp.setOrganizationId("https://ror.org/04aj4c181");
    var location = new OrganizationInfo.Location();
    var address = new OrganizationInfo.Location.Address();
    address.setAddressCountry("DE");
    address.setAddressRegion("Lower Saxony");
    location.setAddress(address);
    resp.setLocations(List.of(location));
    resp.setDateUpdated(LocalDateTime.now());
    return resp;
  }

  @Test
  void testCachedData() {
    when(organizationInfoRepository.findById("https://ror.org/04aj4c181")).thenReturn(Optional.of(getTestData()));
    var result = organizationInfoService.getOrganizationInfo("https://ror.org/04aj4c181");
    assertThat(result).isNotNull();
    verify(rorConnector, never()).loadOrganizationInfo(Mockito.anyString());
  }


  @Test
  void testReloadExpiredCacheData() {
    var data = getTestData();
    data.setDateUpdated(LocalDateTime.of(1990, 1, 1, 0, 0));
    when(organizationInfoRepository.findById("https://ror.org/04aj4c181")).thenReturn(Optional.of(data));
    when(rorConnector.loadOrganizationInfo("https://ror.org/04aj4c181")).thenReturn(data);
    var result = organizationInfoService.getOrganizationInfo("https://ror.org/04aj4c181");
    assertThat(result).isNotNull();
    verify(organizationInfoRepository).save(data);
  }

}

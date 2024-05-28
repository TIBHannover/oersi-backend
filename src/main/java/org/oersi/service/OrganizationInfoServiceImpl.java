package org.oersi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.oersi.connector.OrganizationInfoConnector;
import org.oersi.domain.OrganizationInfo;
import org.oersi.repository.OrganizationInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@PropertySource(value = "file:${envConfigDir:envConf/default/}search_index.properties")
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrganizationInfoServiceImpl implements OrganizationInfoService {

  @Qualifier("ror")
  private final OrganizationInfoConnector rorConnector;
  private final OrganizationInfoRepository organizationInfoRepository;

  @Value("${external_organization_info.cache_expiration}")
  private long cacheExpirationInMinutes;

  @Override
  public OrganizationInfo getOrganizationInfo(String organizationId) {
    if (StringUtils.isEmpty(organizationId)) {
      return null;
    }
    OrganizationInfo cachedResult = loadFromCache(organizationId);
    if (cachedResult != null && cachedResult.getDateUpdated().plusMinutes(cacheExpirationInMinutes).isAfter(LocalDateTime.now())) {
      return cachedResult;
    }
    OrganizationInfo externalResult = loadFromExternal(organizationId);
    if (externalResult != null) {
      log.debug("loaded external result for {}", organizationId);
      storeToCache(externalResult);
    }
    return externalResult;
  }

  private OrganizationInfo loadFromCache(String organizationId) {
    return organizationInfoRepository.findById(organizationId).orElse(null);
  }

  private OrganizationInfo loadFromExternal(String organizationId) {
    if (organizationId.startsWith("https://ror.org/")) {
      return rorConnector.loadOrganizationInfo(organizationId);
    }
    return null;
  }

  private void storeToCache(OrganizationInfo externalResult) {
    externalResult.setDateUpdated(LocalDateTime.now());
    organizationInfoRepository.save(externalResult);
  }

}

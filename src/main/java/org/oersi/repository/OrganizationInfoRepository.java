package org.oersi.repository;

import org.oersi.domain.OrganizationInfo;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface OrganizationInfoRepository extends ElasticsearchRepository<OrganizationInfo, String> {
}

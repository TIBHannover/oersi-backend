package org.sidre.repository;

import org.sidre.domain.OrganizationInfo;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface OrganizationInfoRepository extends ElasticsearchRepository<OrganizationInfo, String> {
}

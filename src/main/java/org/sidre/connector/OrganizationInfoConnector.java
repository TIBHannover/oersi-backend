package org.sidre.connector;

import org.sidre.domain.OrganizationInfo;

public interface OrganizationInfoConnector {

  OrganizationInfo loadOrganizationInfo(String organizationId);

}

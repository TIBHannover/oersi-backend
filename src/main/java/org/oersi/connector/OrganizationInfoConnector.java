package org.oersi.connector;

import org.oersi.domain.OrganizationInfo;

public interface OrganizationInfoConnector {

  OrganizationInfo loadOrganizationInfo(String organizationId);

}

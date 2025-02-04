package org.sidre.service;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sidre.domain.BackendConfig;
import org.sidre.domain.BackendMetadata;
import org.sidre.domain.OembedInfo;
import org.sidre.domain.OrganizationInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@PropertySource(value = "file:${envConfigDir:envConf/default/}search_index.properties")
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Qualifier("amb")
public class AmbMetadataProcessor implements MetadataCustomProcessor {

  private static final String FIELD_NAME_ABOUT = "about";
  private static final String FIELD_NAME_CREATOR = "creator";
  private static final String FIELD_NAME_ENCODING = "encoding";
  private static final String FIELD_NAME_PUBLISHER = "publisher";
  private static final String FIELD_NAME_SOURCE_ORGANIZATION = "sourceOrganization";
  private static final String FIELD_VALUE_ORGANIZATION = "Organization";


  private final @NonNull AmbOembedHelper ambOembedHelper;
  private final @NonNull VocabService vocabService;
  private final @NonNull ConfigService configService;
  private final @NonNull OrganizationInfoService organizationInfoService;

  @Value("${feature.add_missing_metadata_infos}")
  private boolean featureAddMissingMetadataInfos;

  @Value("${feature.amb.add_external_organization_info}")
  private boolean featureAddExternalOrganizationInfo;

  @Override
  public void process(BackendMetadata metadata) {
    addDefaultValues(metadata);
    if (featureAddMissingMetadataInfos) {
      addMissingInfos(metadata);
    }
    if (featureAddExternalOrganizationInfo) {
      addLocationsToPublicOrganizationFields(metadata);
    }
    replaceMultipleRootSubjectsByInterdisciplinaryItem(metadata);
  }

  @Override
  public void postProcess(BackendMetadata metadata) {
    fillInternalIndex(metadata);
  }

  @Override
  public OembedInfo processOembedInfo(OembedInfo oembedInfo, BackendMetadata metadata) {
    return ambOembedHelper.processOembedInfo(oembedInfo, metadata);
  }

  private void addLocationsToPublicOrganizationFields(BackendMetadata metadata) {
    List.of(FIELD_NAME_CREATOR, "creator.affiliation", FIELD_NAME_PUBLISHER, FIELD_NAME_SOURCE_ORGANIZATION)
        .forEach(fieldName ->
            MetadataHelper.modifyObjectTree(metadata.getData(), fieldName, organization -> addOrganizationLocations(organization, false))
        );
  }

  private void addMissingInfos(BackendMetadata metadata) {
    List<Map<String, Object>> encoding = MetadataHelper.parseList(metadata.getData(), FIELD_NAME_ENCODING, new TypeReference<>() {});
    boolean hasEncodingWithEmbedUrl = !CollectionUtils.isEmpty(encoding) && encoding.stream().anyMatch(e -> e.get("embedUrl") != null);
    if (encoding == null) {
      encoding = new ArrayList<>();
    }
    if (!hasEncodingWithEmbedUrl && metadata.getAutoUpdateInfo() != null && metadata.getAutoUpdateInfo().getEmbedUrl() != null) {
      encoding.add(Map.of("embedUrl", metadata.getAutoUpdateInfo().getEmbedUrl(), "type", "MediaObject"));
      metadata.getData().put(FIELD_NAME_ENCODING, encoding);
    }
  }

  private void fillInternalIndex(BackendMetadata metadata) {
    Map<String, Object> internalData = new HashMap<>(metadata.getData());
    List<Map<String, Object>> creators = MetadataHelper.parseList(internalData, FIELD_NAME_CREATOR, new TypeReference<>() {});
    if (creators == null) {
      creators = new ArrayList<>();
    }
    List<Map<String, Object>> persons = creators.stream().filter(c -> "Person".equals(c.get("type"))).toList();
    internalData.put("persons", persons);

    List<Map<String, Object>> institutions = new ArrayList<>();
    institutions.addAll(creators.stream().filter(c -> FIELD_VALUE_ORGANIZATION.equals(c.get("type"))).toList());
    institutions.addAll(creators.stream().filter(c -> c.get("affiliation") != null)
      .map(c -> MetadataHelper.parse(c, "affiliation", new TypeReference<Map<String, Object>>() {}))
      .toList());
    List<Map<String, Object>> sourceOrganization = MetadataHelper.parseList(internalData, FIELD_NAME_SOURCE_ORGANIZATION, new TypeReference<>() {});
    if (sourceOrganization != null) {
      institutions.addAll(sourceOrganization);
    }
    List<Map<String, Object>> publishers = MetadataHelper.parseList(internalData, FIELD_NAME_PUBLISHER, new TypeReference<>() {});
    institutions.addAll(determineInstitutionsForWhitelistedPublisher(publishers));
    if (featureAddExternalOrganizationInfo) {
      institutions.forEach(this::addOrganizationLocations);
    }
    mapInstitutionNameToInternalName(institutions);
    internalData.put("institutions", institutions);

    metadata.setExtendedData(internalData);
  }

  private void addOrganizationLocations(Map<String, Object> organization) {
    addOrganizationLocations(organization, true);
  }

  private void addOrganizationLocations(Map<String, Object> organization, boolean includeGeoPoint) {
    if (!FIELD_VALUE_ORGANIZATION.equals(organization.get("type"))) {
      return;
    }
    OrganizationInfo organizationInfo = organizationInfoService.getOrganizationInfo((String) organization.get("id"));
    if (organizationInfo != null && organizationInfo.getLocations() != null) {
      organization.put("location", organizationInfo.getLocations().stream().map(location -> {
        Map<String, Object> formattedLocation = MetadataHelper.format(location);
        if (!includeGeoPoint) {
          formattedLocation.remove("geo");
        }
        return formattedLocation;
      }).toList());
    }
  }

  @Data
  private static class InstitutionMapping {
    private String regex;
    private Pattern regexPattern;
    private String internalName;
    private boolean copyFromPublisher;
    private String defaultId;
  }
  private interface InstitutionMappingProcessor {
    void process(Map<String, Object> institution, InstitutionMapping mapping);
  }
  private List<InstitutionMapping> getInstitutionMapping() {
    BackendConfig config = configService.getMetadataConfig();
    if (config != null && config.getCustomConfig() != null) {
      List<InstitutionMapping> institutionMapping = MetadataHelper.parseList(config.getCustomConfig(), "institutionMapping", new TypeReference<>() {});
      if (institutionMapping != null) {
        institutionMapping.forEach(m -> m.setRegexPattern(Pattern.compile(m.regex)));
        return institutionMapping;
      }
    }
    return new ArrayList<>();
  }

  private void processInstitutionMapping(List<InstitutionMapping> institutionMappings, List<Map<String, Object>> institutions, InstitutionMappingProcessor processor) {
    if (institutions == null || institutions.isEmpty()) {
      return;
    }
    final List<InstitutionMapping> finalInstitutionMappings = Objects.requireNonNullElseGet(institutionMappings, this::getInstitutionMapping);
    institutions.forEach(institution -> finalInstitutionMappings.stream()
            .filter(mapping -> mapping.regexPattern.matcher((String) institution.get("name")).matches())
            .findFirst()
            .ifPresent(institutionMapping -> processor.process(institution, institutionMapping)));
  }

  private List<Map<String, Object>> determineInstitutionsForWhitelistedPublisher(List<Map<String, Object>> publishers) {
    List<Map<String, Object>> institutions = new ArrayList<>();
    if (publishers == null || publishers.isEmpty()) {
      return institutions;
    }
    List<InstitutionMapping> publisherMappings = getInstitutionMapping().stream().filter(m -> m.copyFromPublisher).toList();
    processInstitutionMapping(publisherMappings, publishers, (institution, mapping) -> institutions.add(institution));
    return institutions;
  }

  private void mapInstitutionNameToInternalName(List<Map<String, Object>> institutions) {
    processInstitutionMapping(null, institutions, (institution, mapping) -> {
      var matcher = mapping.regexPattern.matcher((CharSequence) institution.get("name"));
      if (matcher.matches()) {
        if (mapping.getInternalName() != null) {
          institution.put("name", mapping.getInternalName());
        } else if (matcher.group(1) != null) {
          institution.put("name", matcher.group(1));
        }
      }
    });
  }
  private void addDefaultInstitutionId(List<InstitutionMapping> institutionMappings, List<Map<String, Object>> institutions) {
    processInstitutionMapping(institutionMappings, institutions, (institution, mapping) -> {
      if (institution.get("id") == null && mapping.defaultId != null) {
        institution.put("id", mapping.defaultId);
      }
    });
  }

  private void addDefaultValues(final BackendMetadata metadata) {
    List<String> types = MetadataHelper.parseList(metadata.getData(), "type", new TypeReference<>() {});
    if (types == null || CollectionUtils.isEmpty(types)) {
      metadata.getData().put("type", new ArrayList<>(List.of("LearningResource")));
    }
    if (metadata.get("isAccessibleForFree") == null) {
      metadata.getData().put("isAccessibleForFree", true);
    }
    MetadataHelper.modifyObjectList(metadata.getData(), FIELD_NAME_ENCODING, e -> e.putIfAbsent("type", "MediaObject"));
    MetadataHelper.modifyObjectList(metadata.getData(), "mainEntityOfPage", e -> e.putIfAbsent("type", "WebContent"));
    // default organization IDs
    List<InstitutionMapping> institutionMappings = getInstitutionMapping();
    List.of(FIELD_NAME_CREATOR, FIELD_NAME_SOURCE_ORGANIZATION, FIELD_NAME_PUBLISHER).forEach(fieldName ->
      MetadataHelper.modifyObjectList(metadata.getData(), fieldName, institution -> {
        if (FIELD_VALUE_ORGANIZATION.equals(institution.get("type"))) {
          addDefaultInstitutionId(institutionMappings, List.of(institution));
        }
        if (institution.get("affiliation") != null) {
          MetadataHelper.modifyObject(institution, "affiliation", affiliation -> addDefaultInstitutionId(institutionMappings, List.of(affiliation)));
        }
      })
    );
  }

  private void replaceMultipleRootSubjectsByInterdisciplinaryItem(BackendMetadata data) {
    List<Map<String, Object>> about = MetadataHelper.parseList(data.getData(), FIELD_NAME_ABOUT, new TypeReference<>() {});
    if (about != null) {
      Set<String> ids = about.stream().map(e -> (String) e.get("id")).collect(Collectors.toSet());
      if (ids.size() >= 3) {
        Map<String, String> parentMap = vocabService.getParentMap("hochschulfaechersystematik");
        for (String id : ids) {
          if (parentMap.get(id) != null) {
            return;
          }
        }
        about = List.of(Map.of("id", "https://w3id.org/kim/hochschulfaechersystematik/n0"));
        data.getData().put(FIELD_NAME_ABOUT, about);
      }
    }
  }

  protected void setFeatureAddExternalOrganizationInfo(boolean featureAddExternalOrganizationInfo) {
    this.featureAddExternalOrganizationInfo = featureAddExternalOrganizationInfo;
  }

}

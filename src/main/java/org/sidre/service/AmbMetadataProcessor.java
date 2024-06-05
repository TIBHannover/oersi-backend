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
  private static final String FIELD_NAME_ENCODING = "encoding";
  private static final String FIELD_NAME_PREF_LABEL = "prefLabel";


  private final @NonNull AmbOembedHelper ambOembedHelper;
  private final @NonNull VocabService vocabService;
  private final @NonNull ConfigService configService;
  private final @NonNull OrganizationInfoService organizationInfoService;

  @Value("${feature.add_missing_labels}")
  private boolean featureAddMissingLabels;

  @Value("${feature.add_missing_metadata_infos}")
  private boolean featureAddMissingMetadataInfos;

  @Value("${feature.add_missing_parent_items_of_hierarchical_vocabs}")
  private boolean featureAddMissingParentItems;

  @Value("${feature.amb.add_external_organization_info}")
  private boolean featureAddExternalOrganizationInfo;

  @Override
  public void process(BackendMetadata metadata) {
    addDefaultValues(metadata);
    if (featureAddMissingMetadataInfos) {
      addMissingInfos(metadata);
    }
    replaceMultipleRootSubjectsByInterdisciplinaryItem(metadata);
    if (featureAddMissingParentItems) {
      addMissingParentItemsForHierarchicalVocab(metadata);
    }
    if (featureAddMissingLabels) {
      addMissingLabels(metadata);
    }
    fillInternalIndex(metadata);
  }

  @Override
  public OembedInfo processOembedInfo(OembedInfo oembedInfo, BackendMetadata metadata) {
    return ambOembedHelper.processOembedInfo(oembedInfo, metadata);
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
    List<Map<String, Object>> creators = MetadataHelper.parseList(internalData, "creator", new TypeReference<>() {});
    if (creators == null) {
      creators = new ArrayList<>();
    }
    List<Map<String, Object>> persons = creators.stream().filter(c -> "Person".equals(c.get("type"))).toList();
    internalData.put("persons", persons);

    List<Map<String, Object>> institutions = new ArrayList<>();
    institutions.addAll(creators.stream().filter(c -> "Organization".equals(c.get("type"))).toList());
    institutions.addAll(creators.stream().filter(c -> c.get("affiliation") != null)
      .map(c -> MetadataHelper.parse(c, "affiliation", new TypeReference<Map<String, Object>>() {}))
      .toList());
    List<Map<String, Object>> sourceOrganization = MetadataHelper.parseList(internalData, "sourceOrganization", new TypeReference<>() {});
    if (sourceOrganization != null) {
      institutions.addAll(sourceOrganization);
    }
    List<Map<String, Object>> publishers = MetadataHelper.parseList(internalData, "publisher", new TypeReference<>() {});
    institutions.addAll(determineInstitutionsForWhitelistedPublisher(publishers));
    if (featureAddExternalOrganizationInfo) {
      for (Map<String, Object> institution: institutions) {
        OrganizationInfo organizationInfo = organizationInfoService.getOrganizationInfo((String) institution.get("id"));
        if (organizationInfo != null) {
          institution.put("location", organizationInfo.getLocations());
        }
      }
    }
    mapInstitutionNameToInternalName(institutions);
    internalData.put("institutions", institutions);

    metadata.setExtendedData(internalData);
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
    // default organization IDs
    List<InstitutionMapping> institutionMappings = getInstitutionMapping();
    List.of("creator", "sourceOrganization", "publisher").forEach(fieldName -> {
      MetadataHelper.modifyObjectList(metadata.getData(), fieldName, institution -> {
        if ("Organization".equals(institution.get("type"))) {
          addDefaultInstitutionId(institutionMappings, List.of(institution));
        }
        if (institution.get("affiliation") != null) {
          MetadataHelper.modifyObject(institution, "affiliation", affiliation -> addDefaultInstitutionId(institutionMappings, List.of(affiliation)));
        }
      });
    });
  }

  private List<String> getLabelledConceptFields() {
    BackendConfig config = configService.getMetadataConfig();
    if (config != null && config.getCustomConfig() != null) {
      List<String> fields = MetadataHelper.parseList(config.getCustomConfig(), "labelledConceptFields", new TypeReference<>() {});
      if (fields != null) {
        return fields;
      }
    }
    return new ArrayList<>();
  }

  private Map<String, String> aboutParentMap = null;
  private Map<String, String> getAboutParentMap() {
    if (aboutParentMap == null) {
      aboutParentMap = vocabService.getParentMap("hochschulfaechersystematik");
    }
    return aboutParentMap;
  }
  protected void resetAboutParentMap() {
    aboutParentMap = null;
  }

  private void replaceMultipleRootSubjectsByInterdisciplinaryItem(BackendMetadata data) {
    List<Map<String, Object>> about = MetadataHelper.parseList(data.getData(), FIELD_NAME_ABOUT, new TypeReference<>() {});
    if (about != null) {
      Set<String> ids = about.stream().map(e -> (String) e.get("id")).collect(Collectors.toSet());
      if (ids.size() >= 3) {
        Map<String, String> parentMap = getAboutParentMap();
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

  private void addMissingParentItemsForHierarchicalVocab(BackendMetadata data) {
    List<Map<String, Object>> about = MetadataHelper.parseList(data.getData(), FIELD_NAME_ABOUT, new TypeReference<>() {});
    if (about != null) {
      Set<String> ids = about.stream().map(e -> (String) e.get("id")).collect(Collectors.toSet());
      Set<String> idsToAdd = getParentIdsToAdd(ids, getAboutParentMap());
      for (String id: idsToAdd) {
        about.add(Map.of("id", id));
      }
      data.getData().put(FIELD_NAME_ABOUT, about);
    }
  }
  private Set<String> getParentIdsToAdd(Set<String> ids, Map<String, String> parentMap) {
    Set<String> idsToAdd = new HashSet<>();
    for (String id : ids) {
      String parentId = parentMap.get(id);
      if (parentId != null && !ids.contains(parentId)) {
        idsToAdd.add(parentId);
      }
    }
    if (!idsToAdd.isEmpty()) {
      Set<String> allIds = new HashSet<>();
      allIds.addAll(ids);
      allIds.addAll(idsToAdd);
      idsToAdd.addAll(getParentIdsToAdd(allIds, parentMap));
    }
    return idsToAdd;
  }

  /**
   * Add default localized labels that are not defined at the given metadata.
   * @param metadata set label at this data
   */
  public void addMissingLabels(BackendMetadata metadata) {
    getLabelledConceptFields().forEach(field -> addMissingLabels(metadata, field));
  }

  private void addMissingLabels(BackendMetadata metadata, String fieldName) {
    Map<String, Object> data = metadata.getData();
    if (data.get(fieldName) instanceof List) {
      List<Map<String, Object>> labelledConceptList = MetadataHelper.parseList(data, fieldName, new TypeReference<>() {});
      if (labelledConceptList != null) {
        labelledConceptList.forEach(this::addMissingLabels);
        data.put(fieldName, labelledConceptList);
      }
    } else {
      Map<String, Object> labelledConcept = MetadataHelper.parse(data, fieldName, new TypeReference<>() {});
      if (labelledConcept != null) {
        addMissingLabels(labelledConcept);
        data.put(fieldName, labelledConcept);
      }
    }
  }

  public void addMissingLabels(Map<String, Object> labelledConcept) {
    Map<String, String> existingLabels = MetadataHelper.parse(labelledConcept, FIELD_NAME_PREF_LABEL, new TypeReference<>() {});
    Map<String, String> defaultLocalizedLabel = getDefaultLocalizedLabel((String) labelledConcept.get("id"));
    Map<String, String> prefLabel = existingLabels;
    if (defaultLocalizedLabel != null ) {
      if (prefLabel == null) {
        prefLabel = new HashMap<>();
      }
      for (Map.Entry<String, String> defaultLabelEntry : defaultLocalizedLabel.entrySet()) {
        if (prefLabel.containsKey(defaultLabelEntry.getKey())) {
          continue;
        }
        prefLabel.put(defaultLabelEntry.getKey(), defaultLabelEntry.getValue());
      }
    }
    if (prefLabel != null) {
      labelledConcept.put(FIELD_NAME_PREF_LABEL, prefLabel);
    }
  }

  private Map<String, String> getDefaultLocalizedLabel(String identifier) {
    return vocabService.findLocalizedLabelByIdentifier(identifier);
  }

  protected void setFeatureAddMissingParentItems(boolean featureAddMissingParentItems) {
    this.featureAddMissingParentItems = featureAddMissingParentItems;
  }

  protected void setFeatureAddExternalOrganizationInfo(boolean featureAddExternalOrganizationInfo) {
    this.featureAddExternalOrganizationInfo = featureAddExternalOrganizationInfo;
  }

}

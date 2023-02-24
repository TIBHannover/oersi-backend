package org.oersi.service;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oersi.domain.BackendConfig;
import org.oersi.domain.BackendMetadata;
import org.oersi.domain.OembedInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@PropertySource(value = "file:${envConfigDir:envConf/default/}oersi.properties")
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AmbMetadataProcessor implements MetadataCustomProcessor {

  private static final String FIELD_NAME_ABOUT = "about";
  private static final String FIELD_NAME_ENCODING = "encoding";
  private static final String FIELD_NAME_PREF_LABEL = "prefLabel";


  private final @NonNull AmbOembedHelper ambOembedHelper;
  private final @NonNull LabelDefinitionService labelDefinitionService;
  private final @NonNull VocabService vocabService;
  private final @NonNull LabelService labelService;
  private final @NonNull ConfigService configService;

  @Value("${feature.add_missing_labels}")
  private boolean featureAddMissingLabels;

  @Value("${feature.add_missing_metadata_infos}")
  private boolean featureAddMissingMetadataInfos;

  @Value("${feature.add_missing_parent_items_of_hierarchical_vocabs}")
  private boolean featureAddMissingParentItems;

  @Override
  public void process(BackendMetadata metadata) {
    addDefaultValues(metadata);
    if (featureAddMissingMetadataInfos) {
      addMissingInfos(metadata);
    }
    if (featureAddMissingParentItems) {
      addMissingParentItemsForHierarchicalVocab(metadata);
    }
    if (featureAddMissingLabels) {
      addMissingLabels(metadata);
    }
    fillInternalIndex(metadata);
  }
  @Override
  public void postProcess(BackendMetadata metadata) {
    storeLabels(metadata);
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
    List<Object> persons = creators.stream().filter(c -> "Person".equals(c.get("type"))).collect(Collectors.toList());
    internalData.put("persons", persons);

    List<Object> institutions = new ArrayList<>();
    institutions.addAll(creators.stream().filter(c -> "Organization".equals(c.get("type"))).collect(Collectors.toList()));
    institutions.addAll(creators.stream().filter(c -> c.get("affiliation") != null)
      .map(c -> c.get("affiliation"))
      .collect(Collectors.toList()));
    List<Object> sourceOrganization = MetadataHelper.parseList(internalData, "sourceOrganization", new TypeReference<>() {});
    if (sourceOrganization != null) {
      institutions.addAll(sourceOrganization);
    }
    List<Map<String, Object>> publishers = MetadataHelper.parseList(internalData, "publisher", new TypeReference<>() {});
    institutions.addAll(determineInstitutionsForWhitelistedPublisher(publishers));
    internalData.put("institutions", institutions);

    metadata.setAdditionalData(internalData);
  }

  @Data
  public static class WhitelistMapping {
    private String name;
    private String regex;
  }

  private List<WhitelistMapping> getPublisherToInstitutionWhitelistMapping() {
    BackendConfig config = configService.getMetadataConfig();
    if (config != null && config.getCustomConfig() != null) {
      List<WhitelistMapping> publisherMappings = MetadataHelper.parseList(config.getCustomConfig(), "publisherToInstitutionWhitelist", new TypeReference<>() {});
      if (publisherMappings != null) {
        return publisherMappings;
      }
    }
    return new ArrayList<>();
  }

  private List<Map<String, Object>> determineInstitutionsForWhitelistedPublisher(List<Map<String, Object>> publishers) {
    List<Map<String, Object>> institutions = new ArrayList<>();
    if (publishers == null || publishers.isEmpty()) {
      return institutions;
    }
    List<WhitelistMapping> publisherMappings = getPublisherToInstitutionWhitelistMapping();
    publishers.forEach(publisher -> publisherMappings.forEach(mapping -> {
      String publisherName = (String) publisher.get("name");
      var pattern = Pattern.compile(mapping.getRegex());
      var matcher = pattern.matcher(publisherName);
      if (matcher.matches()) {
        if (mapping.getName() != null) {
          publisher.put("name", mapping.getName());
        } else {
          publisher.put("name", matcher.group(1));
        }
        institutions.add(publisher);
      }
    }));
    return institutions;
  }

  private void addDefaultValues(final BackendMetadata metadata) {
    List<String> types = MetadataHelper.parseList(metadata.getData(), "type", new TypeReference<>() {});
    if (types == null || CollectionUtils.isEmpty(types)) {
      metadata.getData().put("type", new ArrayList<>(List.of("LearningResource")));
    }
    if (metadata.get("isAccessibleForFree") == null) {
      metadata.getData().put("isAccessibleForFree", true);
    }
    List<Map<String, Object>> encoding = MetadataHelper.parseList(metadata.getData(), FIELD_NAME_ENCODING, new TypeReference<>() {});
    if (encoding != null) {
      encoding.stream().filter(e -> e.get("type") == null).forEach(e -> e.put("type", "MediaObject"));
      metadata.getData().put(FIELD_NAME_ENCODING, encoding);
    }
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

  /**
   * Use the @{@link LabelService} to store all labels contained in this @{@link BackendMetadata}.
   * @param metadata metadata
   */
  private void storeLabels(final BackendMetadata metadata) {
    getLabelledConceptFields().forEach(field -> storeLabels(metadata, field));
  }
  private void storeLabels(final BackendMetadata metadata, final String fieldName) {
    Map<String, Object> data = metadata.getData();
    if (data.get(fieldName) instanceof List) {
      List<Map<String, Object>> labelledConceptList = MetadataHelper.parseList(data, fieldName, new TypeReference<>() {});
      if (labelledConceptList != null) {
        labelledConceptList.forEach(l -> storeLabels(l, fieldName));
      }
    } else {
      Map<String, Object> labelledConcept = MetadataHelper.parse(data, fieldName, new TypeReference<>() {});
      if (labelledConcept != null) {
        storeLabels(labelledConcept, fieldName);
      }
    }
  }
  private void storeLabels(Map<String, Object> labelledConcept, final String fieldName) {
    final String key = (String) labelledConcept.get("id");
    final Map<String, String> prefLabel = MetadataHelper.parse(labelledConcept, FIELD_NAME_PREF_LABEL, new TypeReference<>() {});
    if (key == null || prefLabel == null) {
      return;
    }
    for (Map.Entry<String, String> entry : prefLabel.entrySet()) {
      labelService.createOrUpdate(entry.getKey(), key, entry.getValue(), fieldName);
    }
  }

  private Map<String, String> aboutParentMap = null;
  private Map<String, String> getAboutParentMap() {
    if (aboutParentMap == null) {
      aboutParentMap = vocabService.getParentMap("hochschulfaechersystematik");
    }
    return aboutParentMap;
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
    return labelDefinitionService.findLocalizedLabelByIdentifier(identifier);
  }

  protected void setFeatureAddMissingParentItems(boolean featureAddMissingParentItems) {
    this.featureAddMissingParentItems = featureAddMissingParentItems;
  }

}

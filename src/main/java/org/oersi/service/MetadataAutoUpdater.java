package org.oersi.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.oersi.AutoUpdateProperties;
import org.oersi.domain.About;
import org.oersi.domain.Audience;
import org.oersi.domain.ConditionsOfAccess;
import org.oersi.domain.LearningResourceType;
import org.oersi.domain.LocalizedString;
import org.oersi.domain.Media;
import org.oersi.domain.Metadata;
import org.oersi.domain.Provider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Helper class that sets missing infos at {@link org.oersi.domain.Metadata} (like embed-url of known videos, image width and height, provider)
 */
@Service
@PropertySource(value = "file:${envConfigDir:envConf/default/}oersi.properties")
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Setter
public class MetadataAutoUpdater {

  private final @NonNull LabelDefinitionService labelDefinitionService;
  private final @NonNull VocabService vocabService;

  @Value("${feature.add_missing_parent_items_of_hierarchical_vocabs}")
  private boolean featureAddMissingParentItems;

  private final @NonNull AutoUpdateProperties autoUpdateProperties;

  public void addMissingInfos(Metadata data) {
    if (featureAddMissingParentItems) {
      addMissingParentItemsForHierarchicalVocab(data);
    }

    boolean shouldUpdateByDefinitions = !hasEmbedUrl(data) || !hasProviderName(data) || !hasProviderUrl(data);
    if (shouldUpdateByDefinitions) {
      for (AutoUpdateProperties.Entry definition : autoUpdateProperties.getDefinitions()) {
        var pattern = Pattern.compile(definition.getRegex());
        var matcher = pattern.matcher(data.getIdentifier());
        if (matcher.matches()) {
          updateMissingEmbedUrl(data, definition, matcher);
          updateMissingProviderInfo(data, definition);
        }
      }
    }
  }

  private Map<String, String> aboutParentMap = null;
  private Map<String, String> getAboutParentMap() {
    if (aboutParentMap == null) {
      aboutParentMap = vocabService.getParentMap("hochschulfaechersystematik");
    }
    return aboutParentMap;
  }
  private void addMissingParentItemsForHierarchicalVocab(Metadata data) {
    if (data.getAbout() != null) {
      Set<String> ids = data.getAbout().stream().map(About::getIdentifier).collect(Collectors.toSet());
      Set<String> idsToAdd = getParentIdsToAdd(ids, getAboutParentMap());
      for (String id: idsToAdd) {
        About about = new About();
        about.setIdentifier(id);
        data.getAbout().add(about);
      }
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

  private void updateMissingProviderInfo(Metadata data, AutoUpdateProperties.Entry definition) {
    boolean existingProviderName = hasProviderName(data);
    boolean existingProviderUrl = hasProviderUrl(data);
    if (existingProviderName && existingProviderUrl) {
      return; // provider infos already there
    }
    Provider provider = data.getProvider();
    if (provider == null) {
      provider = new Provider();
      data.setProvider(provider);
    }
    if (definition.getProviderName() != null && !existingProviderName) {
      provider.setName(definition.getProviderName());
      log.debug("provider name {}", provider.getName());
    }
    if (definition.getProviderUrl() != null && !existingProviderUrl) {
      provider.setIdentifier(definition.getProviderUrl());
      log.debug("provider url {}", provider.getIdentifier());
    }
  }

  private void updateMissingEmbedUrl(Metadata data, AutoUpdateProperties.Entry definition, Matcher matcher) {
    if (hasEmbedUrl(data)) {
      return; // embed url already set -> skip
    }
    var embedUrl = definition.getEmbedUrl();
    if (embedUrl == null) {
      return; // no embed url in this definition -> skip
    }
    log.debug("{} matches {}", definition.getRegex(), data.getIdentifier());
    for (int i = 1; i <= matcher.groupCount(); i++) {
      embedUrl = embedUrl.replace("###" + i + "###", matcher.group(i));
    }
    log.debug("embed url {}", embedUrl);
    List<Media> encoding = data.getEncoding();
    if (encoding == null) {
      encoding = new ArrayList<>();
      data.setEncoding(encoding);
    }
    Media videoEncoding = new Media();
    videoEncoding.setEmbedUrl(embedUrl);
    encoding.add(videoEncoding);
  }

  private boolean hasProviderName(Metadata data) {
    return data.getProvider() != null && data.getProvider().getName() != null;
  }
  private boolean hasProviderUrl(Metadata data) {
    return data.getProvider() != null && data.getProvider().getIdentifier() != null;
  }
  private boolean hasEmbedUrl(Metadata data) {
    return data.getEncoding() != null && data.getEncoding().stream().anyMatch(e -> e.getEmbedUrl() != null);
  }

  /**
   * Add default localized labels that are not defined at the given metadata.
   * @param metadata set label at this data
   */
  public void addMissingLabels(Metadata metadata) {
    if (metadata.getAbout() != null) {
      for (About about : metadata.getAbout()) {
        about.setPrefLabel(addMissingLabels(about.getIdentifier(), about.getPrefLabel()));
      }
    }
    if (metadata.getAudience() != null) {
      for (Audience audience : metadata.getAudience()) {
        audience.setPrefLabel(addMissingLabels(audience.getIdentifier(), audience.getPrefLabel()));
      }
    }
    if (metadata.getConditionsOfAccess() != null) {
      ConditionsOfAccess coa = metadata.getConditionsOfAccess();
      coa.setPrefLabel(addMissingLabels(coa.getIdentifier(), coa.getPrefLabel()));
    }
    if (metadata.getLearningResourceType() != null) {
      for (LearningResourceType lrt : metadata.getLearningResourceType()) {
        lrt.setPrefLabel(addMissingLabels(lrt.getIdentifier(), lrt.getPrefLabel()));
      }
    }
  }

  public LocalizedString addMissingLabels(String identifier, LocalizedString existingLabels) {
    Map<String, String> defaultLocalizedLabel = getDefaultLocalizedLabel(identifier);
    LocalizedString result = existingLabels;
    if (defaultLocalizedLabel != null ) {
      if (result == null) {
        result = new LocalizedString();
        result.setLocalizedStrings(new HashMap<>());
      } else if (result.getLocalizedStrings() == null) {
        result.setLocalizedStrings(new HashMap<>());
      }
      Map<String, String> localizedStrings = result.getLocalizedStrings();
      for (Map.Entry<String, String> defaultLabelEntry : defaultLocalizedLabel.entrySet()) {
        if (localizedStrings.containsKey(defaultLabelEntry.getKey())) {
          continue;
        }
        localizedStrings.put(defaultLabelEntry.getKey(), defaultLabelEntry.getValue());
      }
    }
    return result;
  }

  private Map<String, String> getDefaultLocalizedLabel(String identifier) {
    return labelDefinitionService.findLocalizedLabelByIdentifier(identifier);
  }

}

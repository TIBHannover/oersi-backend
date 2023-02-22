package org.oersi.service;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.oersi.domain.BackendMetadata;
import org.oersi.domain.OembedInfo;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AmbOembedHelper {

  public OembedInfo processOembedInfo(OembedInfo oembedInfo, BackendMetadata data) {
    oembedInfo.setTitle((String) data.get("name"));
    List<Map<String, Object>> creators = MetadataHelper.parseList(data.getData(), "creator", new TypeReference<>() {});
    if (!CollectionUtils.isEmpty(creators)) {
      oembedInfo.setAuthors(creators.stream().map(this::convertToOembedAuthor).collect(Collectors.toList()));
    }

    Map<String, Object> license = MetadataHelper.parse(data.getData(), "license", new TypeReference<>() {});
    if (license != null) {
      oembedInfo.setLicenseUrl((String) license.get("id"));
    }

    oembedInfo.setThumbnailUrl((String) data.get("image"));

    List<Map<String, Object>> videoEmbeddings = getEncodingsWithEmbedUrl(data);
    if (!videoEmbeddings.isEmpty()) {
      oembedInfo.setVideoEmbedUrl((String) videoEmbeddings.get(0).get("embedUrl"));
    }

    return oembedInfo;
  }

  private List<Map<String, Object>> getEncodingsWithEmbedUrl(BackendMetadata data) {
    List<Map<String, Object>> encoding = MetadataHelper.parseList(data.getData(), "encoding", new TypeReference<>() {});
    if (!CollectionUtils.isEmpty(encoding)) {
      return encoding.stream()
        .filter(e -> e.get("embedUrl") != null)
        .collect(Collectors.toList());
    }
    return new ArrayList<>();
  }

  private OembedInfo.Author convertToOembedAuthor(Map<String, Object> creator) {
    var author = new OembedInfo.Author();
    author.setName((String) creator.get("name"));
    author.setUrl((String) creator.get("id"));
    return author;
  }
}

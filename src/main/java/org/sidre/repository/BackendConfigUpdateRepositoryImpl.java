package org.sidre.repository;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sidre.domain.BackendConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;


import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BackendConfigUpdateRepositoryImpl implements BackendConfigUpdateRepository {

  private static final ObjectMapper objectMapper = getObjectMapper();
  private final @NonNull ElasticsearchOperations elasticsearchOperations;

  private static ObjectMapper getObjectMapper() {
    return JsonMapper.builder()
        .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
        .changeDefaultPropertyInclusion(incl -> incl.withContentInclusion(JsonInclude.Include.NON_NULL))
        .build();
  }

  @Override
  public BackendConfig createOrUpdate(BackendConfig o) {
    return createOrUpdate(o.getId(), o, BackendConfig.class);
  }

  public <T> T createOrUpdate(String id, T o, Class<T> clazz) {
    Document doc = Document.from(objectMapper.convertValue(o, new TypeReference<Map<String, Object>>() {}));
    IndexOperations indexOperations = elasticsearchOperations.indexOps(clazz);
    UpdateQuery query = UpdateQuery.builder(id).withDocument(doc).withDocAsUpsert(true).build();
    elasticsearchOperations.update(query, indexOperations.getIndexCoordinates());
    indexOperations.refresh();
    return elasticsearchOperations.get(id, clazz);
  }
}

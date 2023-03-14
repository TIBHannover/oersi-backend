package org.oersi.service;

import lombok.extern.slf4j.Slf4j;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaClient;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.oersi.domain.BackendMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
@PropertySource(value = "file:${envConfigDir:envConf/default/}oersi.properties")
@Slf4j
public class MetadataValidator {

  private final Schema baseSchema;
  private final Schema schema;

  public MetadataValidator(@Value("${metadata.schema.location}") String schemaLocation, @Value("${metadata.schema.resolution_scope}") String schemaResolutionScope, @Value("classpath:schemas/base/schema.json") Resource rawBaseSchema, ResourceLoader resourceLoader) throws IOException {
    Resource rawSchema = resourceLoader.getResource(schemaLocation);
    baseSchema  = loadSchema(rawBaseSchema, "classpath://schemas/base/");
    schema = loadSchema(rawSchema, schemaResolutionScope);
  }

  private Schema loadSchema(Resource rawSchema, String scope) throws IOException {
    SchemaLoader schemaLoader = SchemaLoader.builder()
      .schemaJson(new JSONObject(new JSONTokener(rawSchema.getInputStream())))
      .schemaClient(SchemaClient.classPathAwareClient())
      .resolutionScope(scope)
      .build();
    return schemaLoader.load().build();
  }

  public ValidatorResult validate(BackendMetadata metadata) {
    return validate(schema, metadata.getData());
  }

  public ValidatorResult validateBaseFields(Map<String, Object> data) {
    return validate(baseSchema, data);
  }

  private ValidatorResult validate(Schema s, Map<String, Object> data) {
    ValidatorResult result = new ValidatorResult();
    try {
      s.validate(new JSONObject(data));
    } catch (ValidationException e) {
      e.getAllMessages().forEach(result::addViolation);
    }
    return result;
  }

}

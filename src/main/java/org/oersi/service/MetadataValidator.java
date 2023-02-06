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
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class MetadataValidator {

  private final Schema schema;

  public MetadataValidator(@Value("classpath:schemas/schema.json") Resource rawSchema) throws IOException {
    schema = loadSchema(rawSchema);
  }

  private Schema loadSchema(Resource rawSchema) throws IOException {
    SchemaLoader schemaLoader = SchemaLoader.builder()
      .schemaJson(new JSONObject(new JSONTokener(rawSchema.getInputStream())))//
      .schemaClient(SchemaClient.classPathAwareClient())//
      .resolutionScope("classpath://schemas/")//
      .build();
    return schemaLoader.load().build();
  }

  public ValidatorResult validate(BackendMetadata metadata) {
    ValidatorResult result = new ValidatorResult();
    try {
      schema.validate(new JSONObject(metadata.getData()));
    } catch (ValidationException e) {
      e.getAllMessages().forEach(result::addViolation);
    }
    return result;
  }

}

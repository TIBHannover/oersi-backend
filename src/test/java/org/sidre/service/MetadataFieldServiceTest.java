package org.sidre.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sidre.ElasticsearchServicesMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class MetadataFieldServiceTest {

  @Nested
  @SpringBootTest(properties = {
          "base-field-config.metadataSource.field=metadataSource", "base-field-config.metadataSource.objectIdentifier=id",
          "base-field-config.metadataSource.useMultipleItems=true", "base-field-config.metadataSource.isObject=true",
          "base-field-config.metadataSource.queries[0].name=providerName", "base-field-config.metadataSource.queries[0].field=provider.name"
  })
  @Import(ElasticsearchServicesMock.class)
  class MetadataSourceObjectListTest {
    @Autowired
    MetadataFieldService metadataFieldService;

    @Test
    void testMetadataSourceObjectList() {
      List<MetadataFieldService.MetadataSourceItem> metadataSourceItems = metadataFieldService.getMetadataSourceItems(Map.of(
              "id", "12345",
              "metadataSource", List.of(
                      Map.of("id", "12345")
              ))
      );
      Assertions.assertEquals(1, metadataSourceItems.size());
      Assertions.assertEquals(Map.of("id", "12345"), metadataSourceItems.get(0).getValue());
    }

    @Test
    void testGetMetadataSourceValues() {
      MetadataFieldService.MetadataSourceItem metadataSourceItem = new MetadataFieldService.MetadataSourceItem(Map.of("id", "12345", "provider", Map.of("name", "test")));
      List<String> values = metadataFieldService.getValues(metadataSourceItem, "metadataSource.provider.name");
      Assertions.assertEquals(List.of("test"), values);
    }

    @Test
    void testGetMetadataSourceValuesWithMissingValues() {
      MetadataFieldService.MetadataSourceItem metadataSourceItem = new MetadataFieldService.MetadataSourceItem(Map.of("id", "12345"));
      List<String> values = metadataFieldService.getValues(metadataSourceItem, "metadataSource.provider.name");
      Assertions.assertTrue(values.isEmpty());
    }

    @Test
    void testUpdateMetadataSources() {
      Map<String, Object> data = new HashMap<>(Map.of(
              "id", "12345",
              "metadataSource", List.of(Map.of("id", "12345"), Map.of("id", "56789"))
      ));
      MetadataFieldService.MetadataSourceItem metadataSourceItem = new MetadataFieldService.MetadataSourceItem(Map.of("id", "56789"));
      metadataFieldService.updateMetadataSource(data, List.of(metadataSourceItem));
      Assertions.assertEquals(Map.of("id", "12345","metadataSource", List.of(Map.of("id", "56789"))), data);
    }
  }

  @Nested
  @SpringBootTest(properties = {
          "base-field-config.metadataSource.field=metadataSource", "base-field-config.metadataSource.objectIdentifier=id",
          "base-field-config.metadataSource.useMultipleItems=false", "base-field-config.metadataSource.isObject=true",
          "base-field-config.metadataSource.queries[0].name=providerName", "base-field-config.metadataSource.queries[0].field=provider.name"
  })
  @Import(ElasticsearchServicesMock.class)
  class MetadataSourceSingleObjectTest {
    @Autowired
    MetadataFieldService metadataFieldService;

    @Test
    void testMetadataSourceObjectList() {
      List<MetadataFieldService.MetadataSourceItem> metadataSourceItems = metadataFieldService.getMetadataSourceItems(Map.of(
              "id", "12345",
              "metadataSource", Map.of("id", "12345"))
      );
      Assertions.assertEquals(1, metadataSourceItems.size());
      Assertions.assertEquals(Map.of("id", "12345"), metadataSourceItems.get(0).getValue());
    }

    @Test
    void testGetMetadataSourceValues() {
      MetadataFieldService.MetadataSourceItem metadataSourceItem = new MetadataFieldService.MetadataSourceItem(Map.of("id", "12345", "provider", Map.of("name", "test")));
      List<String> values = metadataFieldService.getValues(metadataSourceItem, "metadataSource.provider.name");
      Assertions.assertEquals(List.of("test"), values);
    }

    @Test
    void testUpdateMetadataSources() {
      Map<String, Object> data = new HashMap<>(Map.of(
              "id", "12345",
              "metadataSource", Map.of("id", "12345")
      ));
      MetadataFieldService.MetadataSourceItem metadataSourceItem = new MetadataFieldService.MetadataSourceItem(Map.of("id", "56789"));
      metadataFieldService.updateMetadataSource(data, List.of(metadataSourceItem));
      Assertions.assertEquals(Map.of("id", "12345","metadataSource", Map.of("id", "56789")), data);
    }
  }

  @Nested
  @SpringBootTest(properties = {
          "base-field-config.metadataSource.field=info.metadataSource", "base-field-config.metadataSource.objectIdentifier=id",
          "base-field-config.metadataSource.useMultipleItems=true", "base-field-config.metadataSource.isObject=true",
          "base-field-config.metadataSource.queries[0].name=providerName", "base-field-config.metadataSource.queries[0].field=provider.name"
  })
  @Import(ElasticsearchServicesMock.class)
  class MetadataSourceComplexObjectListTest {
    @Autowired
    MetadataFieldService metadataFieldService;

    @Test
    void testMetadataSourceObjectList() {
      List<MetadataFieldService.MetadataSourceItem> metadataSourceItems = metadataFieldService.getMetadataSourceItems(Map.of(
              "id", "12345",
              "info", Map.of("metadataSource", List.of(Map.of("id", "12345")))
      ));
      Assertions.assertEquals(1, metadataSourceItems.size());
      Assertions.assertEquals(Map.of("id", "12345"), metadataSourceItems.get(0).getValue());
    }

    @Test
    void testGetMetadataSourceValues() {
      MetadataFieldService.MetadataSourceItem metadataSourceItem = new MetadataFieldService.MetadataSourceItem(Map.of("id", "12345", "provider", Map.of("name", "test")));
      List<String> values = metadataFieldService.getValues(metadataSourceItem, "info.metadataSource.provider.name");
      Assertions.assertEquals(List.of("test"), values);
    }

    @Test
    void testGetMetadataSourceValuesWithMissingValues() {
      MetadataFieldService.MetadataSourceItem metadataSourceItem = new MetadataFieldService.MetadataSourceItem(Map.of("id", "12345"));
      List<String> values = metadataFieldService.getValues(metadataSourceItem, "info.metadataSource.provider.name");
      Assertions.assertTrue(values.isEmpty());
    }

    @Test
    void testUpdateMetadataSources() {
      Map<String, Object> data = new HashMap<>(Map.of(
              "id", "12345",
              "info", Map.of("metadataSource", List.of(Map.of("id", "12345"), Map.of("id", "56789")))
      ));
      MetadataFieldService.MetadataSourceItem metadataSourceItem = new MetadataFieldService.MetadataSourceItem(Map.of("id", "56789"));
      metadataFieldService.updateMetadataSource(data, List.of(metadataSourceItem));
      Assertions.assertEquals(Map.of("id", "12345","info", Map.of("metadataSource", List.of(Map.of("id", "56789")))), data);
    }
  }

  @Nested
  @SpringBootTest(properties = {
          "base-field-config.metadataSource.field=metadataSource", "base-field-config.metadataSource.objectIdentifier=",
          "base-field-config.metadataSource.useMultipleItems=true", "base-field-config.metadataSource.isObject=false",
  })
  @Import(ElasticsearchServicesMock.class)
  class MetadataSourceValueListTest {
    @Autowired
    MetadataFieldService metadataFieldService;

    @Test
    void testMetadataSourceValueList() {
      List<MetadataFieldService.MetadataSourceItem> metadataSourceItems = metadataFieldService.getMetadataSourceItems(Map.of(
              "id", "12345",
              "metadataSource", List.of("12345", "67890"))
      );
      Assertions.assertEquals(2, metadataSourceItems.size());
      Assertions.assertEquals("12345", metadataSourceItems.get(0).getValue());
      Assertions.assertEquals("67890", metadataSourceItems.get(1).getValue());
    }

    @Test
    void testGetMetadataSourceValues() {
      MetadataFieldService.MetadataSourceItem metadataSourceItem = new MetadataFieldService.MetadataSourceItem("12345");
      List<String> values = metadataFieldService.getValues(metadataSourceItem, "metadataSource");
      Assertions.assertEquals(List.of("12345"), values);
    }

    @Test
    void testUpdateMetadataSources() {
      Map<String, Object> data = new HashMap<>(Map.of(
              "id", "12345",
              "metadataSource", List.of("12345", "67890")
      ));
      MetadataFieldService.MetadataSourceItem metadataSourceItem = new MetadataFieldService.MetadataSourceItem("67890");
      metadataFieldService.updateMetadataSource(data, List.of(metadataSourceItem));
      Assertions.assertEquals(Map.of("id", "12345","metadataSource", List.of("67890")), data);
    }
  }

  @Nested
  @SpringBootTest(properties = {
          "base-field-config.metadataSource.field=metadataSource", "base-field-config.metadataSource.objectIdentifier=",
          "base-field-config.metadataSource.useMultipleItems=false", "base-field-config.metadataSource.isObject=false",
  })
  @Import(ElasticsearchServicesMock.class)
  class MetadataSourceSingleValueTest {
    @Autowired
    MetadataFieldService metadataFieldService;

    @Test
    void testMetadataSourceSingleValue() {
      List<MetadataFieldService.MetadataSourceItem> metadataSourceItems = metadataFieldService.getMetadataSourceItems(Map.of(
              "id", "12345",
              "metadataSource", "12345")
      );
      Assertions.assertEquals(1, metadataSourceItems.size());
      Assertions.assertEquals("12345", metadataSourceItems.get(0).getValue());
    }

    @Test
    void testGetMetadataSourceValues() {
      MetadataFieldService.MetadataSourceItem metadataSourceItem = new MetadataFieldService.MetadataSourceItem("12345");
      List<String> values = metadataFieldService.getValues(metadataSourceItem, "metadataSource");
      Assertions.assertEquals(List.of("12345"), values);
    }

    @Test
    void testUpdateMetadataSources() {
      Map<String, Object> data = new HashMap<>(Map.of(
              "id", "12345",
              "metadataSource", "12345"
      ));
      MetadataFieldService.MetadataSourceItem metadataSourceItem = new MetadataFieldService.MetadataSourceItem("67890");
      metadataFieldService.updateMetadataSource(data, List.of(metadataSourceItem));
      Assertions.assertEquals(Map.of("id", "12345","metadataSource", "67890"), data);
    }
  }

  @Nested
  @SpringBootTest(properties = {
          "base-field-config.metadataSource.field=metadataSource", "base-field-config.metadataSource.objectIdentifier=",
          "base-field-config.metadataSource.useMultipleItems=false", "base-field-config.metadataSource.isObject=false",
  })
  @Import(ElasticsearchServicesMock.class)
  class MetadataSourceValuesTest {

    @Test
    void testSimpleValue() {
      List<String> values = MetadataFieldServiceImpl.getValuesFromObject(
              Map.of("test", "a"),
              "test"
      );
      Assertions.assertEquals(List.of("a"), values);
    }
    @Test
    void testListValues() {
      List<String> values = MetadataFieldServiceImpl.getValuesFromObject(
              Map.of("test", List.of("a", "b")),
              "test"
      );
      Assertions.assertEquals(List.of("a", "b"), values);
    }
    @Test
    void testComplexValue() {
      List<String> values = MetadataFieldServiceImpl.getValuesFromObject(
              Map.of("test", Map.of("z", "a")),
              "test.z"
      );
      Assertions.assertEquals(List.of("a"), values);
    }
    @Test
    void testComplexListValues() {
      List<String> values = MetadataFieldServiceImpl.getValuesFromObject(
              Map.of("test", List.of(Map.of("z", "a"), Map.of("z", "b"), Map.of("z", "c"))),
              "test.z"
      );
      Assertions.assertEquals(List.of("a", "b", "c"), values);
    }
  }

}

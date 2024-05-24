# Search Index Backend

Backend / API of the Search Index for internal use. Provides access to the metadata index. Read data without authentification. Crud-operations to metadata authenticated.

The (backend) API is not part of the public API. It is designed to be consumed by the other search index components (etl, frontend,...) or by a custom component (like a custom frontend).

## Configuration

* Set up the configuration directory
    * _logback.xml_ - Logging-configuration
    * _search_index.properties_ - configuration of the application
    * see example in _envConf/default_

* set configuration directory with _envConfigDir=PATH_
    * for example via _context.xml.default_ in Tomcat
            
            <Context>
            	<!-- path to the config-directory that contains all config-files of the application -->
            	<Environment type="java.lang.String" name="envConfigDir" value="/some/path/conf/" override="false"/>
            </Context>

    * or via Command-Line-Argument
    
            mvn spring-boot:run -Dspring-boot.run.arguments=--envConfigDir=/soma/path/conf

## Local Development Environment

To set up a local development environment that allows you to run the app locally via `mvn spring-boot:run` or execute unit-tests locally, you need an elasticsearch-instance. Easiest would be to start a local elasticsearch-instance via Docker:
```
docker run -it --rm --name elasticsearch -p 9200:9200 -e "ES_JAVA_OPTS=-Xms2g -Xmx2g" -e "discovery.type=single-node" -e "xpack.security.enabled=false" docker.elastic.co/elasticsearch/elasticsearch:7.17.7
```

## Supported Metadata Schema

You need to specify the base schema fields of your schema in _search_index.properties_.
* `base-field-config.resourceIdentifier` - (required) the field containing the unique identification of the resource
* `base-field-config.metadataSource` - (optional) subfields define the usage of the source metadata information. If absent, no features (like deletions) via source-metadata-id is available.
* `base-field-config.metadataSource.field` - the field containing the metadata source information
* `base-field-config.metadataSource.useMultipleItems` - (true/false) contains the specified field a single value or multiple values
* `base-field-config.metadataSource.isObject` - (true/false) contains the specified field an object or (a) string value(s)
* `base-field-config.metadataSource.objectIdentifier` - the identifier of the object item (isObject=true). base is the object item field. Only required for isObject=true, otherwise the raw value is used as identifier.
* `base-field-config.metadataSource.queries` - define a list of named queries to be able to access object resources by a metadataSource search
* `base-field-config.metadataSource.queries.name` - the name of the query
* `base-field-config.metadataSource.queries.field` - the search field of the query

## Rest API

API definition in [src/main/resources/model/api.yaml](src/main/resources/model/api.yaml)

#### Endpoints
* **_SearchController_**: Read-Access to the index data **/api/search/**
    * Sets a user that has read-only access to the elasticsearch metadata index and execute the request in elasticsearch (GET, POST).
    * Use directly the elasticsearch API - see [Elasticsearch Search API](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-search.html) and [Elasticsearch Query DSL](https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-script-query.html)
    * default value for public address: **/resources/api/search/oer_data/**
    * example `curl -L oersi.org/resources/api/search/oer_data/_search`
* **_MetadataController_**: CRUD-operations to the metadata **/api/metadata/**
    * metadata schema is configurable via `metadata.schema.location` and `metadata.schema.resolution_scope` in _search_index.properties_
        * default schema in [oersi-schema](https://gitlab.com/oersi/oersi-schema) ([conversion](https://gitlab.com/oersi/oersi-backend/-/issues/8#note_344342881))
        * custom processor for amb-schema can be configured via `metadata.custom.processor=amb` in _search_index.properties_
    * bulk-update and -deletion via **/api/metadata/bulk**. Recommended bulk-update-size: 25
* **_LabelController_**: Retrieve labels for controlled vocabularies **/api/label/**
    * In the data there are some fields that use controlled vocabularies with labels (for example `learningResourceType` or `about`) -> these labels can be accessed here directly
    * Labels can be retrieved for a given language - and optionally for a given field/group.
    * Provides a Map **LabelKey** -> **LabelValue** as result in format Json
* **_ContactController_**: Create contact requests **/api/contact/**
    * Internal use - this is not part of the public API
    * User messages can be sent via Mail to the support address of the search index instance
        * Configure `spring.mail`-Properties and `search_index.support.mail` for this in _search_index.properties_
* **_oEmbedController_**: Provide an [oEmbed](https://oembed.com/) API **/api/oembed-json** and **/api/oembed-xml**
    * supports only types `video` and `link` at the moment
    * thumbnails are available whenever the `image` at the resource is available and the dimension match
    * additional response parameters are provided
        * **license_url** - URL of the license of the resource
        * **authors** - array of authors (better usage for multiple authors)
    * default value for public adress: **/resources/api/oembed-json** respectively **/resources/api/oembed-xml**
    * example `curl -L oersi.org/resources/api/oembed-json?url=https%3A%2F%2Foersi.org%2Fresources%2FaHR0cHM6Ly9heGVsLWtsaW5nZXIuZ2l0bGFiLmlvL2dpdGxhYi1mb3ItZG9jdW1lbnRzL2luZGV4Lmh0bWw%3D`

#### Interactive documentation
* An interactive documentation of the API can be found at ``http://<YOUR-HOST>:8080/search-index-backend/swagger-ui.html`` (adjust tomcat port, application name if the standard values were not used)
    * You need to have access to the internal search index system. The interactive swagger documentation is not available in the web.
    * use [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) for an application started locally with ``mvn spring-boot:run``

#### OpenAPI (Swagger) Configuration

* [OpenAPI](https://swagger.io/docs/specification/basic-structure/) is an open-source software framework backed by a large ecosystem of tools that helps developers design, build, document, and consume RESTful web services
* The API is completely defined in a **Yaml** file. Swagger generates all java components from this file (like the data transfer objects (DTO, Model) and Controller). So if you want to modify, or create a new one (Model or Controller), adjust the **Yaml** in [src/main/resources/model/api.yaml](src/main/resources/model/api.yaml)

## Features

#### Default Labels

* Activate via feature-toggle `feature.add_missing_labels`.
* If active, set all prefLabels during the metadata creation/update that are not included in the given data, but that are defined in `VocabItem` (see also `vocab` endpoint in API).

#### Auto Update missing infos

* Activate via feature-toggle `feature.add_missing_metadata_infos`. 
* If active, set embed url during the metadata creation/update. Only missing data will be set. Update by rules that apply if the id matches a regex: Is configured in the _search_index.properties_ -> **autoupdate**-properties

## Technologies

* **springboot** - The backend is a springboot application, provided as war file
* **spring-security** - Secure write-operations to index data
* **project lombok** - Automatically generate code like getter, setter, equals, hashcode,...
     * Set up your IDE: [https://projectlombok.org/setup/overview](https://projectlombok.org/setup/overview)
* **modelmapper** - Automatic mapping between DTOs and Entities
* **swagger** -  design, build, document, and consume RESTful web services

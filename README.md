# OER Search Index Backend

Backend / API of the OER Search Index for internal use. Provides access to the oer index data. Read data without authentification. Crud-operations to oer sql data authenticated.

The (backend) API is not part of the oersi public API. It is designed to be consumed by the other oersi components (etl, frontend,...) or by a custom component (like a custom frontend).

## Configuration

* Set up the configuration directory
    * _logback.xml_ - Logging-configuration
    * _oersi.properties_ - configuration of the application
    * see example in _envConf/default_

* set configuration directory with _envConfigDir=PATH_
    * for example via _context.xml.default_ in Tomcat
            
            <Context>
            	<!-- path to the config-directory that contains all config-files of the application -->
            	<Environment type="java.lang.String" name="envConfigDir" value="/some/path/conf/" override="false"/>
            </Context>

    * or via Command-Line-Argument
    
            mvn spring-boot:run -Dspring-boot.run.arguments=--envConfigDir=/soma/path/conf

## Rest API

API definition in [src/main/resources/model/api2.yaml](src/main/resources/model/api2.yaml)

#### Endpoints
* **_SearchController_**: Read-Access to the index data **/api/search/**
    * Sets a user that has read-only access to the elasticsearch index **oer_data** and execute the request in elasticsearch (GET, POST).
    * Use directly the elasticsearch API - see [Elasticsearch Search API](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-search.html) and [Elasticsearch Query DSL](https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-script-query.html)
    * default value for public adress: **/resources/api-internal/search/oer_data/**
    * example `curl -L oersi.de/resources/api-internal/search/oer_data/_search`
* **_MetadataController_**: CRUD-operations to the sql data **/api/metadata/**
    * based on https://dini-ag-kim.github.io/lrmi-profile/draft/schemas/schema.json ([conversion](https://gitlab.com/oersi/oersi-backend/-/issues/8#note_344342881))
* **_LabelController_**: Retrieve labels from the data **/api/label/**
    * Internal use - this is not part of the public API
    * In the data there are labels for some fields (for example `learningResourceType` or `about`) -> these labels can be accessed here directly
    * Labels can be retrieved for a given language - and optionally for a given field/group.

#### Interactive documentation
* An interactive documentation of the API can be found at ``http://<YOUR-HOST>:8080/oersi/swagger-ui.html`` (adjust tomcat port, application name if the standard values were not used)
    * You need to have access to the internal oersi system. The interactive swagger documentation is not available in the web.
    * use [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) for an application started locally with ``mvn spring-boot:run``

#### Swagger Configuration

* [Swagger](https://swagger.io/docs/specification/2-0/basic-structure/) is an open-source software framework backed by a large ecosystem of tools that helps developers design, build, document, and consume RESTful web services
* The API is completely defined in a **Yaml** file. Swagger generates all java components from this file (like the data transfer objects (DTO, Model) and Controller). So if you want to modify, or create a new one (Model or Controller), adjust the **Yaml** in [src/main/resources/model/api2.yaml](src/main/resources/model/api2.yaml)
       
## Technologies

* **springboot** - The backend is a springboot application, provided as war file
* **liquibase** - Automatically manage database updates
* **spring-security** - Secure write-operations to oer index data
* **project lombok** - Automatically generate code like getter, setter, equals, hashcode,...
     * Set up your IDE: [https://projectlombok.org/setup/overview](https://projectlombok.org/setup/overview)
* **modelmapper** - Automatic mapping between DTOs and Entities
* **swagger** -  design, build, document, and consume RESTful web services
                                    

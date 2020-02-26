# OER Search Index Backend

Backend / API of the OER Search Index. Provides access to the oer index data. Read data without authentification. Crud-operations to oer index data authenticated.

## Configuration

* Set up the configuration directory
    * _logback.xml_ - Logging-configuration
    * oersi.properties_ - configuration of the application
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

* Read-Access to the index data via _SearchController_ **/api/search**
* CRUD-operations via _MetadataController_ **/api/metadata**

## Technologies

* **springboot** - The backend is a springboot application, provided as war file
* **liquibase** - Automatically manage database updates
* **spring-security** - Secure write-operations to oer index data
* **project lombok** - Automatically generate code like getter, setter, equals, hashcode,...
     * Set up your IDE: [https://projectlombok.org/setup/overview](https://projectlombok.org/setup/overview)
* **modelmapper** - Automatic mapping between DTOs and Entities

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
* The documentation of the API can be found at _http://&lt;YOUR-HOST&gt;/&lt;YOUR-APP-ROOT&gt;/swagger-ui.html_
    * use [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) for an application started with ``mvn spring-boot:run``

### Rest API Swagger Configuration

* Swagger is an open-source software framework backed by a large ecosystem of tools that helps developers design, build, document, and consume RESTful web services

* Swagger Generate DTO (Model) and Controller , so if you want to modify , or create a new one (Model or Controller ), add in **Yaml**  
 
    *  **Yam fIle is in :**
            
          > ./src/main/resorces/model/api2.yaml

    *  **After each time the yaml file is changed , need to run the command**
          
          > mvn compile
          

## Technologies

* **springboot** - The backend is a springboot application, provided as war file
* **liquibase** - Automatically manage database updates
* **spring-security** - Secure write-operations to oer index data
* **project lombok** - Automatically generate code like getter, setter, equals, hashcode,...
     * Set up your IDE: [https://projectlombok.org/setup/overview](https://projectlombok.org/setup/overview)
* **modelmapper** - Automatic mapping between DTOs and Entities
* **swagger** -  design, build, document, and consume RESTful web services
                                    
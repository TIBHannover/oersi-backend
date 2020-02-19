# OER Search Index Backend

Backend / API of the OER Search Index

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


## Development

### Project lombok
Project lombok is used by this project. Set up your IDE: [https://projectlombok.org/setup/overview](https://projectlombok.org/setup/overview)
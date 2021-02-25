# WaCoDiS Datasource Observer
![Build](https://github.com/WaCoDiS/datasource-observer/workflows/Build/badge.svg)  
Observer component for data model supported SubsetJobDefinitions.
  
The WaCoDiS Datasource Observer component provides routines for handling WaCoDiS job creation and deletion events. As each WaCoDiS job requires concrete datasets of supported datasources (e.g. Copernicus satellite data or sensor data) to start its result computation, the role of the data source observer is to query and observe each required datasource so that relevant metadata about relevant datasets can be stored and used in subsequent steps of the WaCoDiS pipeline.

**Table of Content**  
1. [WaCoDiS Project Information](#wacodis-project-information)
2. [Overview](#overview) 
3. [Installation / Building Information](#installation--building-information)
4. [Deployment](#deployment)
6. [Developer Information](#developer-information)
7. [Contact](#contact)
8. [Credits and Contributing Organizations](#credits-and-contributing-organizations)

## WaCoDiS Project Information
<p align="center">
  <img src="https://raw.githubusercontent.com/WaCoDiS/apis-and-workflows/master/misc/logos/wacodis.png" width="200">
</p>
Climate changes and the ongoing intensification of agriculture effect in increased material inputs in watercourses and dams.
Thus, water industry associations, suppliers and municipalities face new challenges. To ensure an efficient and environmentally
friendly water supply for the future, adjustments on changing conditions are necessary. Hence, the research project WaCoDiS
aims to geo-locate and quantify material outputs from agricultural areas and to optimize models for sediment and material
inputs (nutrient contamination) into watercourses and dams. Therefore, approaches for combining heterogeneous data sources,
existing interoperable web based information systems and innovative domain oriented models will be explored.

### Architecture Overview

For a detailed overview about the WaCoDiS system architecture please visit the 
**[WaCoDiS Core Engine](https://github.com/WaCoDiS/core-engine)** repository.  

## Overview  
The WaCoDiS Datasource Observer is capable of querying metadata about concrete datasets of required datasources. Currently supported datasources comprise (the list can be extended through implementation of the relevant source code interfaces):

 - **Copernicus satellite** data from either 
    - SentinelHub
    - Code-DE
 - **Sensor Data** served by standardized Sensor Observation Service APIs
 - **Weather Information** available from German weather service (DWD)
 
For each supported datasource additional spatio-temporal information is required so the Datasource Observer may know which concrete datasets must be queried. In essence, the following aspects are consumed:

 - spatial bounding box of the area of interest
 - temporal parameters (point/period in time)
 
 Within the WaCoDiS system, the Datasource Observer subscribes to message broker *WacodisJobDefinition* creation/deletion events sent by the [Job Definition API](https://github.com/WaCoDiS/job-definition-api). Each *WacodisJobDefinition* includes all necessary information about the previously mentioned spatio-temporal aspects as well as details about required datasets from the upper listed datasources (so-called WaCoDiS *SubsetDefinitions*). 
 
On *WacodisJobDefinition* creation events the Datasource Observer analyzes the job details and may start to observe the relevent datasources, i.e. query metadata about actual datasets for the job specific spatio-temporal and Subset-depending settings.
According to the various *WacoidsJobDefinition* settings these observations may vary between single-time-execution events or regularly repeated processes, observing processes might be started immediately or in the future, and data queries might cover datasets from the past or current datasets.

As soon as relevant datasets are identified the queried dataset metadata is sent as *DataEnvelope* event to the message broker. Subscribed components (i.e. [WaCoDiS Metadata Connector](https://github.com/WaCoDiS/metadata-connector)) pick up and process that information (e.g. check whether metadata for the found datasets have been already registered at the 
**[Data Access API](https://github.com/WaCoDiS/data-access-api)**)

Regarding the number of *observation jobs* compared to the number of *WacodisJobDefinitions*, there is not necessarily a 1:1 relationship. Instead, the Datasource Observer compares the observing characteristics of new *WacodisJobDefinitions* to already running *observation jobs*. Only if a different datasource or disjoint area of interest is demanded, a new separate *observation job* is started. Otherwise an already existing *observation job* is simply enhanced by the new *WacodisJobDefinition* details, i.e. by storing its unique job ID within a list of associated *WacodisJobDefinition* IDs and - if required - expand intersecting bounding boxes of the respective areas of interest.

On *WacodisJobDefinition* deletion events, any associated running *observation job* is updated. The association to the deleted *WacodisJobDefinition* is removed. If no other existing *WacodisJobDefinition* is still associated to the *observation job*, it will be stopped. Otherwise, the observation bounding box is recalculated from the remaining other associated *WacodisJobDefinitions*.

### Modules
The WaCoDiS Datasource Observer comprises seven Maven modules:
* __WaCoDiS Datasource Observer Models__  
This module contains Java classes that reflect the basic data model. This includes the data types specified with OpenAPI
in the WaCoDiS apis-and workflows repository. All model classes were generated by the OpenAPI Generator, which is integrated
and can be used as Maven Plugin within this module.  
* __WaCoDiS Datasource Observer Core__  
This core module contains the main logic to receive and react upon *WacodisJobDefinition* creation/delete message broker events. This comprises *WacodisJobDefinition* validation of observer relevant aspects, *observer jobs* management (creation, enhancing/expanding/shrinking of spatial bounding box for new/deleted similar *WacodisJobDefinition*, deletion), triggering of *observer job* execution (as single-time-execution or repeated execution) and message broker publishing of found actual dataset metadata as *DataEnvelope*. Remarkably, the core module provides generic interface ``JobFactory`` to handle the creation of *observation jobs* for performing the actual datasource queries. The concrete implementation of supported datasources is realized by separate Maven modules (see below).
* __WaCoDiS Datasource Observer App__ 
Since WaCoDiS Datasource Observer is implemented as Spring Boot application, the App module provides the application runner
as well as default externalized configurations.
* __WaCoDiS Datasource Observer Code-DE Observer__ 
Implementation of the core interface ``JobFactory`` providing support for querying Copernicus satellite scenes from the Code-DE platform.
* __WaCoDiS Datasource Observer Sentinel Observer__ 
Implementation of the core interface ``JobFactory`` providing support for querying Copernicus satellite scenes from the SentinelHub platform.
* __WaCoDiS Datasource Observer DWD Observer__ 
Implementation of the core interface ``JobFactory`` providing support for querying weather data from the German weather data provider DWD.
* __WaCoDiS Datasource Observer Sensor Web Observer__ 
Implementation of the core interface ``JobFactory`` providing support for querying sensor data from standardized Sensor Observation Services.


### Technologies
* __Java__  
WaCoDiS Datasource Observer is tested with Oracle JDK 8 and OpenJDK 8. Unless stated otherwise later Java versions can be used as well.
* __Maven__  
This project uses the build-management tool [Apache Maven](https://maven.apache.org/)
* __Spring Boot__  
WaCoDiS Datasource Observer is a standalone application built with the [Spring Boot](https://spring.io/projects/spring-boot) 
framework. Therefore, it is not necessary to deploy WaCoDiS Datasource Observer manually with a web server.  
* __Spring Cloud__  
[Spring Cloud](https://spring.io/projects/spring-cloud) is used for exploiting some ready-to-use features in order to implement
an event-driven workflow. In particular, [Spring Cloud Stream](https://spring.io/projects/spring-cloud-stream) is used
for subscribing to asynchronous messages within the WaCoDiS system.
* __RabbitMQ__  
For communication with other WaCoDiS components of the WaCoDiS system the message broker [RabbitMQ](https://www.rabbitmq.com/)
is utilized. RabbitMQ is not part of WaCoDiS Datasource Observer and therefore [must be deployed separately](#dependencies).
* __OpenAPI__  
[OpenAPI](https://github.com/OAI/OpenAPI-Specification) is used for the specification of data models used within this project.
* __Quartz__  
[Quartz](http://www.quartz-scheduler.org/) is a Java API for execution of recurrent, regular tasks based on a [Cron definition (crontab)](http://pubs.opengroup.org/onlinepubs/9699919799/utilities/crontab.html#tag_20_25_07). Quartz is used for scheduling the observation jobs.

## Installation / Building Information
### Build from Source
In order to build the WaCoDiS Datasource Observer from source _Java Development Kit_ (JDK) must be available. Data Access 
is tested with Oracle JDK 8 and OpenJDK 8. Unless stated otherwise later JDK versions can be used.  

Since this is a Maven project, [Apache Maven](https://maven.apache.org/) must be available for building it. Then, you
can build the project by running `mvn clean install` from root directory

### Build using Docker
The project contains a Dockerfile for building a Docker image. Simply run `docker build -t wacodis/datasource-observer:latest .`
in order to build the image. You will find some detailed information about running the Datasource Observer as Docker container
within the [deployment section](#run-with-docker).

### Configuration
Configuration is fetched from [WaCoDiS Config Server](https://github.com/WaCoDiS/config-server). If config server is not
available, configuration values located at *src/main/resources/application.yml* within the Datasource Observer App submodule
are applied instead.   
#### Parameters
WaCoDiS Datasource Observer is a Spring Boot application and provides an _application.yml_ within the datasource-observer-app
module for configuration purpose. A documentation for common application properties can be found at
https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-application-properties.html

In addition, some configuration parameters relate to different Spring Cloud components, i.e. [Spring Cloud Stream](https://docs.spring.io/spring-cloud-stream/docs/current/reference/html/)
and [Spring Cloud Config](https://docs.spring.io/spring-cloud-config/docs/current/reference/html/).

The following section contains descriptions for configuration parameters structured by configuration section.

##### spring/cloud/stream/bindings/job-creation
configuration of message channel for receiving messages when a new job is created

| value     | description       | note  |
| ------------- |-------------| -----|
| destination     | topic used for messages about created WaCoDiS jobs | e.g. *wacodis.test.jobs.new* |
| binder      | defines the binder (message broker)   | |
| content-type      | content type of  the messages   | should always be *application/json* |

##### spring/cloud/stream/bindings/job-deletion
configuration of message channel for receiving messages when an existing job is deleted

| value     | description       | note  |
| ------------- |-------------| -----|
| destination     | topic used for messages about deleted WaCoDiS jobs | e.g. *wacodis.test.jobs.deleted* |
| binder      | defines the binder (message broker)   | |
| content-type      | content type of  the messages   | should always be *application/json* |

##### spring/cloud/stream/bindings/output-data-envelope
configuration of message channel for publishing messages on newly available dataset metadata as *DataEnvelope*

| value     | description       | note  |
| ------------- |-------------| -----|
| destination     | topic used to receive messages on newly available datasets, must be aligned with Metadata Connector API config | e.g. *wacodis.test.data.available* |
| binder      | defines the binder (message broker)   | |
| content-type      | content type of  DataEnvelope acknowledgement messages (mime type)   | should always be *application/json* |

##### spring/cloud/config
configuration of target URL of a running config server serving dynamic app config file

| value     | description       | note  |
| ------------- |-------------| -----|
| url     | target URL to running config server that holds config file for Datasource Observer App | e.g. *http:localhost:8888* |

##### spring/rabbitmq
parameters related to WaCoDis message broker

| value     | description       | note  |
| ------------- |-------------| -----|
| host | RabbitMQ host (WaCoDiS message broker) | e.g. *localhost* |
| port | RabbitMQ port (WaCoDiS message broker)   | e.g. *5672*|
| username | RabbitMQ username (WaCoDiS message broker)   | |
| password | RabbitMQ password (WaCoDiS message broker)   | |

##### spring/datasource.core.quartz-data-source
Per default, Quartz scheduler makes use of an in-memory job store. In order to configure a JDBC-based store, Core Engine
provides a `DataSource` bean which can be configured using `spring.datasource.core.quartz-data-source`. To configure the
`DataSource` just follow the [Spring Boot guide](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#howto-configure-a-datasource).

##### spring/quartz
Quartz scheduler related beans are provided using [spring-boot-starter-quartz](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-quartz).
Hence, the scheduler can be configured via externalized configuration using `spring.quartz.properties`. Just set the usual
[Quartz configuration properties](https://www.quartz-scheduler.org/documentation/quartz-2.3.0/configuration/).

##### datasource-observer/execution/interval
parameters related to WaCoDis Datasource Observer execution interval settings

| value     | description       | note  |
| ------------- |-------------| -----|
| sentinel | execution interval for re-triggering sentinel-based observation jobs periodically in seconds | e.g. *3600* ~ 5 minutes |
| sensorWeb | execution interval for re-triggering Sensor Observation Service based observation jobs periodically in seconds | e.g. *3600* ~ 5 minutes |
| dwd | execution interval for re-triggering weather data (German DWD) based observation jobs periodically in seconds | e.g. *3600* ~ 5 minutes |

##### datasource-observer/sentinelhub
parameters related to WaCoDis Datasource Observer SentinelHub settings

| value     | description       | note  |
| ------------- |-------------| -----|
| enabled | boolean true or false to enable SentineHub based Copernicus data observation (either SentinelHub or Code-DE can be activated, not both at the same time) | e.g. *text* |
| base-url | url to entrypoint of SentinelHub API | e.g. *https://scihub.copernicus.eu/apihub/* |
| user | username for SentinelHub requests |  |
| password | password for SentinelHub requests |  |

##### datasource-observer/code-de
parameters related to WaCoDis Datasource Observer SentinelHub settings

| value     | description       | note  |
| ------------- |-------------| -----|
| enabled | boolean true or false to enable Code-DE based Copernicus data observation (either SentinelHub or Code-DE can be activated, not both at the same time) | e.g. *text* |

## Deployment
### Dependencies
WaCoDiS Datasource Observer requires a running RabbitMQ instance for consuming messages as well as a running
WaCoDiS **Job Definition API** for receiving *WacodisJobDefinition* creation/deletion event. Furthermore, outgoing *DataEnevlop* messages sent by the Datasource Observer against a dedicated rabbitMQ topic, are picked up by the **Metadata Connector** (However, a running Metadata Connector is not required to run the Datasource Observer). For starting a RabbitMQ instance as Docker container
a _docker-compose.yml_ is provided at _./docker/rabbitmq_. Detailed deployment instructions for the **Job Definition Api** can 
be found at [https://github.com/WaCoDiS/job-definition-api](https://github.com/WaCoDiS/job-definition-api). Same applies for the [https://github.com/WaCoDiS/metadata-connector](https://github.com/WaCoDiS/metadata-connector).

### Run with Maven
Just start the application by running `mvn spring-boot:run` from the root of the `metadata-connector-app` module. Make
sure you have installed all dependencies with `mvn clean install` from the project root.

### Run with Docker
For convenience, a _docker-compose.yml_ is provided for running the Datasource Observer as Docker container. Just, run 
`docker-compose up` from the project root. The latest Docker image will be fetched from [Docker Hub](https://hub.docker.com/repository/docker/wacodis/datasource-observer).
The _docker-compose.yml_ also contains the most important configuration parameters as environment variables. Feel free
to adapt the parameters for your needs.

## Developer Information

### Developer guidelines
The role of the Datasource Observer is to fetch metadata about concrete datasets from supported datasources (satellite data, weather data, sensor data) required for at least one *WacodisJobDefinition*. To be precise, a *WacodisJobDefinition* specifies one ore more required data inputs from supported datasource types (data source types are called *SubsetDefinitions* with respect to the WaCoDiS data model). In addition, spatio-temporal parameters are submitted for each input data type (or globally) to be considered within the data queries performed by the Datasource Observer. datasources. With regard implementations of the model class ``AbstractSubsetDefinition``, the Datasource Observer supports the following dataset types and is able to perform queries against the respective datasource.
`AbstractSubsetDefinition` model class implementations
* `CopernicusSubsetDefinition`: Describes a Copernicus resource e.g., provided at CODE-DE or SentinelHub.
* `DwdSubsetDefinition`: Describes datasets provided by the German Weather Services via WFS.
* `SensorWebSubsetDefinition`: Describes a standardized Sensor Observtion Service resource.

For each of the previously listed supported datasource types, the Datasource Observer provides implementations of its core interface
`JobFactory`, which internally relies on the Java Quartz library to trigger and schedule the actual datasource queries by implementing associated Quartz ``Job`` class. Current implementations include: 
* `CodeDeJobFactory` and ``CodeDeJob``: Implements data queries against Copernicus satellite datasets from CODE-DE platform.
* `SentinelJobFactory` and `SentinelJob`: Implements data queries against Copernicus satellite datasets from SentinelHub platform.
* `SensorWebFactory` and ``SensorWebJob``: Implements data queries against standardized Sensor Observtion Services.
* `DwdJobFactory` and ``DwdJob``: Implements data queries against weather data provided by the German Weather Services via WFS.

It is possible to enhance Datasource Observer for handling additional dataset types. To do so, the following steps are 
required:
 1. Make sure that the datasource type is present within the WaCoDiS data model as implementation of the abstract model ``AbstractSubsetDefinition``. 
    1. In order to be consistent with other WaCoDiS components regarding the support for different data types, we strongly recommend generating the new model classes from an [OpenAPI definition](https://github.com/WaCoDiS/apis-and-workflows/tree/master/openapi).
If not already done, first define your new _DataEnvelope_ within the OpenAPI document.
    2. The [Datasource Observer Models module](data-models) provides Maven profiles for automatically
generating model classes from an OpenAPI document. The _generate-models_ profile generates the models from a Maven
artifact you first have to create for the [OpenAPI definition](https://github.com/WaCoDiS/apis-and-workflows/tree/master/openapi)
project. By using the _download-generate-models_ profile there is no need to create the artifact in beforehand, since
the execution of this profile will download the latest OpenAPI definitions and then creates the models on top of it. 
You can trigger the profiles by respectively running `mvn clean compile -Pgenerate-models` and
`mvn clean compile -Pdownload-generate-models`.
 2. Implement the core module interface `JobFactory` and associated Quartz's ``Job`` class.
    1. It is advised to encapsulate the implementation within a separate Maven module to keep it clean from the core code and focus the code on the actual data query means. 
    2. For examples inspect the already existing Maven Modules ``code-de-observer``, ``sentinel-observer``, ``dwd-observer`` or ``sensor-web-observer``
    3. edit the root ``pom.xml`` and include the new Maven module in the ``modules`` section


### How to contribute
Feel free to implement missing features by creating a pull request. For any feature requests or found bugs, we kindly
ask you to create an issue. 

### Branching
The master branch provides sources for stable builds. The develop branch represents the latest (maybe unstable)
state of development.

### License and Third Party Lib POM Plugins
TODO

## Contributing Developers
|    Name   |   Organization    |    GitHub    |
| :-------------: |:-------------:| :-----:|
| Sebastian Drost | 52° North GmbH | [SebaDro](https://github.com/SebaDro) |
| Arne Vogt | 52° North GmbH | [arnevogt](https://github.com/arnevogt) |
| Christian Danowski-Buhren | Bochum University of Applied Sciences | [cDanowski](https://github.com/cDanowski) |
| Matthes Rieke | 52° North GmbH | [matthesrieke](https://github.com/matthesrieke) |

## Contact
The WaCoDiS project is maintained by [52°North GmbH](https://52north.org/). If you have any questions about this or any
other repository related to WaCoDiS, please contact wacodis-info@52north.org.

## Credits and Contributing Organizations
- Department of Geodesy, Bochum University of Applied Sciences, Bochum
- 52° North Initiative for Geospatial Open Source Software GmbH, Münster
- Wupperverband, Wuppertal
- EFTAS Fernerkundung Technologietransfer GmbH, Münster

The research project WaCoDiS is funded by the BMVI as part of the [mFund programme](https://www.bmvi.de/DE/Themen/Digitales/mFund/Ueberblick/ueberblick.html)  
<p align="center">
  <img src="https://raw.githubusercontent.com/WaCoDiS/apis-and-workflows/master/misc/logos/mfund.jpg" height="100">
  <img src="https://raw.githubusercontent.com/WaCoDiS/apis-and-workflows/master/misc/logos/bmvi.jpg" height="100">
</p>

Job Matching Pilot
==================

[Job Matching Pilot](http://job-matching.semic.eu/) is a pilot project commissioned by [Interoperability Solutions for European Public Administrations - ISA](http://ec.europa.eu/isa/) to demonstrate the added value of [ESCO](https://ec.europa.eu/esco/home).
The main demonstration with this pilot is the analyse of job vacancies and curricula in order to better understand the skill needs on the european labour market.

Downloading the source code
---------------------------
The source code is available at [https://github.com/tenforce/JobMatching.git]().

Source Code Structure
---------------------
The application contains a couple modules which have their own functionality.

* afepa-demo:
  This is an application that can be used to create a cv based on an active running ESCO-service-api.
* esco-job-cv-matching:
  This module contains all the tools used to convert the job and cv input to the data needed for the semic-pilot visualisation.
* semic-pilot:
  Holds the application that shows the results of the annotating and mapping process of the job-matching application.


Running the application
-----------------------
The dependency management of the application is done in gradle. At this moment we are using gradle 2.4

First the application need to be build

```bash
gradle clean build
```

This will generate artifacts that can be used to run the web applications.
There are 2 types of artifact: a war that can be placed in a webserver like tomcat and a jar that start a webcontainer by itself as it is using spring-boot.

```
<project>/afepa-demo/build/libs/afepa-demo.war
<project>/semic-pilot/build/libs/semic-pilot.war
```

```
<project>/afepa-demo/build/libs/afepa-demo.jar
<project>/semic-pilot/build/libs/semic-pilot-0.0.1.jar
```

The tools used to run the conversion of raw data to the input needed for the semic-pilot, can be found in the test folder of the module: esco-job-cv-matching.
Mind, cv and job vacancies have been manually annotated.


Caution
-------
The application uses a version of [ESCO-taxonomy](https://ec.europa.eu/esco/home) that is still actively developed.
Some libraries used are part of the ESCO taxonomy management system and they are, today, not publicly available. 
Please send a request to the [ESCO secretariat](https://ec.europa.eu/esco/contact) for more information (https://ec.europa.eu/esco/contact).

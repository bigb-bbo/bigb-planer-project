BigB Planer

Project including 
- gradle build concepts 
- using open liberty server and
- an in-memory database
- define unit tests for all features
- try to reach a coverage of over 90%
- based on domain driven design concepts, for
- generating a planing sheet (usable for trainings, ...)
- use RESTful service to access the planing functionality
- use DTOs for data exchange together with MapStruct to map domain objects to and from DTOs

FIXED ISSUES:
- fix libertyStart command DONE
  - DONE by previously executing "installFeatures" task
- fix libertyRun/libertyDev command (wrong port? deploy applications?)
  - DONE by adding server.xml file under liberty folder with adapted port
- introduce RESTful service
  - DONE from here https://github.com/OpenLiberty/demo-devmode
    - also BUILD process of liberty needs to be adapted since (old) tests are executed !!!
  - issue SOLVED:
    - running server had error message:
      [FEHLER  ] CWWKZ0013E: Es ist nicht möglich, zwei Anwendungen mit dem Namen bigb-planer-project zu starten.
      solved by removing <webApplication location="bigb-planer-project.war" contextRoot="${app.context.root}"/> from server.xml
    - removed "--scan" from libertyDev run configuration to fix error when stopping server
  - now at least the REST-services under /api can be found, for example "Hello World" on http://laptop-bigb:9980/bigb-planer-project/api/hello
  - or system properties under http://laptop-bigb:9980/bigb-planer-project/api/properties
- my application can now be accessed under http://laptop-bigb:9980/bigb-planer-project/api/planer

Current issues:
- none
 
TODOs:
- create initial version of getting a plan of rounds...
- add Test classes before implementing
  - ...

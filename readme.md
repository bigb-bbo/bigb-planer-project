BigB Planer

Project including 
- gradle build concepts 
- using open liberty server and
- an in-memory database
- define unit tests for all features
- try to reach a coverage of over 90%
- based on domain driven design concepts, for
- generating a planing sheet (usable for trainings, ...)

ToDos
- fix libertyStart command DONE
  - by previously executing "installFeatures" task
- fix libertyRun/libertyDev command (wrong port? deploy applications?)
  - by adding server.xml file under liberty folder with adapted port
- add Test classes before implementing
# Backend for TimeSY
## Compile:
Set the templates folder in application.properties. <br>
Initially run ```mvn clean verify -U``` to verify all dependencies get installed. <br>
Then use ```mvn spring-boot:run``` to run the Backend.

## Endpoints:
- main url: ```/api-timesy```
- ```/rooms``` get a list of all available rooms sorted by buildings (get endpoint)
  - parameters:
    - building
    - floor
    - roomUid
    - roomName
    - courseUid
    - courseName
- ```/rooms/<uid>``` update the template for the given room (patch endpoint)
  - body:
    - templateUid
- ```/templates``` get all available templates (get endpoint)
- ```/templates/update``` re-read the template folder and get all available templates (get endpoint)
- ```/display/update``` force update displays (get endpoint)
  - parameters:
    - roomUid (if not set, all displays will be updated)
- ```/dummydata``` Creates a set of dummydata in the DB for testing (get endpoint)
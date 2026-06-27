# Backend for TimeSY
## Compile:
If on Linux, install playwright dependencies with ```npx playwright install-deps``` (you might need to install npm first). <br>
Copy ```src/main/resources/application.properties.default``` to ```src/main/resources/application.properties```. <br>
Set the templates folder and the keycloak secret in application.properties. <br>
Initially run ```mvn clean verify -U``` to verify all dependencies get installed. <br>
Then use ```mvn spring-boot:run``` to run the Backend.

## Run:
The HeOnline mock needs to be running as well: https://github.com/SWB4-Projektarbeit/HE-Online-Mock

## Templates:
Templates need to have an `index.html` and a `metadata.json` which needs to contain the following:
```
{
  "template_uid": <uid>,
  "template_name": <name>
}
```
Once a template finished rendering, it should log `template rendered` to the console to let the backend know, the screenshot can be taken.

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
    - roomType
- ```/rooms/<uid>``` update the template for the given room (patch endpoint)
  - body:
    - templateUid
- ```/templates``` get all available templates (get endpoint)
- ```/templates/update``` re-read the template folder and get all available templates (get endpoint)
- ```/templates/data/<roomUid>``` get the data for the dynamic templates for this specific room (get endpoint)
- ```/display/update``` force update displays (get endpoint)
  - parameters:
    - roomUid (if not set, all displays will be updated)
- ```/dummydata``` Creates a set of dummydata in the DB for testing (get endpoint)

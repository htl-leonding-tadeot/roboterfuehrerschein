# PimpedHotRoad
## Dependencies
To package the `PimpedHOTRoad` Desktop application the following dependencies are required:
* `maven`
* `java` (java `21` is known to work)

## Running the app
* The desktop client can be launched by executing
`mvn clean javafx:run`
* NOTE: Make sure `JAVA_HOME` is set to the correct java installation, otherwise this will not work

## Packaging: create a fat-jar
* To package the application into a single fat jar run `mvn compile package`
* The resulting jar-file can now be found under `./shade/` and can be executed as usual with `java -jar <jar-file>`
* NOTE: The created jar-file is not cross-platform
* NOTE2: The required configuration for packaging a non-modular javafx application was taken from [here](https://github.com/openjfx/samples/tree/f44ca19e4178ee01663d7a2ae8a5da8e58eee1fa/CommandLine/Non-modular/Maven) example project


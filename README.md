# Getting Started

This program produces transcripts in the current directory in PDF format with the naming convention P<campus id>-<sequence number>.pdf
Database connection properties are specified in build.properties.  Build properties must specify the values of the following variables (user and password must be specified in the example below):
* database.powerCampus.driver=com.microsoft.sqlserver.jdbc.SQLServerDriver
* database.powerCampus.user=
* database.powerCampus.password=
* database.powerCampus.url=jdbc:sqlserver://Playground;ServerName=playground.lcunet.lcu.edu;databaseName=Campus6;instanceName=Testing


### To run one transcript
 mvn spring-boot:run -Dspring-boot.run.arguments=P000181686

### To run first transcript
 mvn spring-boot:run 

### To run all transcripts
mvn spring-boot:run -Dspring-boot.run.arguments=--all
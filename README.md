# Yogi

Vocabulary trainer. For Germans to learn e.g. English. User interface is german.
Implemented with Spring Boot 2.5, Thymeleaf and Bootstrap 5.

## Building

`mvn clean package`

## Run locally

... with example vocabulary.

* `mvn spring-boot:run`
* point your browser to `http://localhost:8080` and 
  login with the credentials from src/test/etc/user.properties
* stop with ctrl-c

## Run container locally

... with example vocabulary.

* `mvn clean package -Pdocker`
* `docker run -it -v$(pwd)/src/test/etc:/app/etc -v$(pwd)/.yogilib:/app/yogilib -p8080:8080 ghcr.io/mlhartme/yogi:latest`
* point your browser to `http://localhost:8080` and login with the credentials from src/test/etc/user.properties
* stop with ctrl-c


## Run in Kubernetes via Helm - deploy to yogi.schmizzolin.de

* setup Helm, Kubernetes, ...
* adjust .values-override.yaml as needed
* `helm install yogi oci://ghcr.io/mlhartme/charts/yogi --version 1.4.1-20221009-191944`
* point your browser to https://yogi.schmizzolin.de/

See src/charts/values.yaml for available options


## Directory structure for running application

${yogi.user}                user.properties
${yogi.lib}                 persistent state -- system property, default is .yogilib
  - <user>                  userFiles
    - <book>
      - *.selection         Arbitrary Selection
      - 1.protocol          Übung 1
      - 2.protocol          Übung 2
      - ...
<cwd>                       container sets working directory to /usr/local/yogi/run (TODO: yogi.run property)
  - work    Tomcat work mkdir
  - logs    Tomcat access logs

Note that books are no longer represented in the filesystem, the are loaded from github releases.

## Container directory structure

/app
  - classes
  - libs
  - resources
/usr              (normal Linux)
/etc              (normal Linux)
...

## Books

are stored in <name>.yogi files. These files are line based, a line is either
* empty
* a header - starts with a hash "#"
* an entry <left>=<right>

Rules
* input is trimmed of whitespace; otherwise, it's matched exactly. That means
  * it is cases sensitive
  * punctation has to be matched exactly
    * make sure the question punctation matches the punctation in the answer
* 3 times in english - e.g:
   bend, bent, bent = hinunterbeugen (3 Zeiten)



# Yogi

Vocabulary trainer. For Germans to learn e.g. English. User interface is german.
Implemented with Spring Boot 2.5, Thymeleaf and Bootstrap 5.

## Building

`mvn clean package`

(or `mvn clean package -P\!kubernetes` to build without Docker/Kubernetes stuff)

## Run locally

... with example vocabulary.

* `mvn spring-boot:run`
* point your browser to `http://localhost:8080` and 
  login with the credentials from src/test/etc/user.properties
* stop with ctrl-c

## Run container locally

... with example vocabulary.

* `docker run -it -p8080:8080 -v/Users/mhm/Projects/github.com/mlhartme/yogi:/usr/local/yogi/run ghcr.io/mlhartme/yogi:1.4.1-20221009-165518`
* point your browser to `http://localhost:8080` and
  login with the credentials from src/test/etc/user.properties
* stop with ctrl-c


## Run in Kubernetes via Helm - deploy to yogi.schmizzolin.de

* setup Helm, Kubernetes, ...
* ./src/git-ssh 
* `helm upgrade yogi oci://ghcr.io/mlhartme/charts/yogi --version 1.4.1-20221009-191944`
* point your browser to https://yogi.schmizzolin.de/

See src/charts/values.yaml for available options


## Directory structure for running application

${yogi.etc}                 configuration -- system property, default is ./src/test/etc
  - user.properties
  - books                   available books
     - <book-1.yogi>
     - <book-1.png>
     - ...
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



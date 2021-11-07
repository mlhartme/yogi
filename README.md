# Yogi

Vocabulary trainer. For Germans to learn e.g. English. User interface is german.
Implemented with Spring Boot 2.5, Thymeleaf and Bootstrap 5.

## Build and run locally

... with example vocabulary.

* `mvn clean package spring-boot:run`
* point your browser to `http://localhost:8080` and login with the credentials from src/test/user.properties
* stop with ctrl-c

## Run with Kubernetes

* setup kubernetes, e.g. via minikube
* `mvn clean package -Pk8s`
* `helm install target/helm`


## Directory structure for running application

${yogi.etc}
  - user.properties
  - books                   available books
     - <book-1.yogi>
     - <book-1.png>
     - ...
${yogi.lib}                 persistent stage
  - <user>                  userFiles
    - <book>
      - *.selection         Arbitrary Selection
      - 1.protocol          Übung 1
      - 2.protocol          Übung 2
      - ...

The docker container also creates

<cwd>       (/usr/local/yogi/run)
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



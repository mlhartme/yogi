# Yogi

Vocabulary trainer. For Germans to learn e.g. English. Implemented with springboot, thymeleaf and bootstrap.

## Build and run locally

* `mvn clean package spring-boot:run`
* point your browser to `http://localhost:8080` and login with the credentials from src/test/user.properties
* stop with ctrl-c

## Run locally with Docker

* setup docker
* `mvn clean package -Pdocker`
* ./run.sh


## Directory structure for running application

${yogi.config}
  - books                   available books
  - user.properties

${yogi.lib}                 persistent stageÜbungsprotokolle
  - <user>                  userFiles
    - <book>
      - *.selection         Arbitrary Selection
      - 1.protocol          Übung 1
      - 2.protocol          Übung 2
      - ...

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


## Server setup

TODO: service still needed?

To create a systemd service on a Linux box

* install docker and docker-compose
* `scp -r src/systemd yourhost:~/yogi/`
* on *yourhost*
  * `mkdir ~/yogi/run`
  * setup `~/yogi/etc` with a ´books` subdirectory and your books
  * `ln -s ~/yogi/systemd/yogi.service /etc/systemd/system
  * `systemctl daemon-reload`
  * `systemctl start yogi`
  * `systemctl enable yogi`


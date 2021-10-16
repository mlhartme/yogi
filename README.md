# Yogi

Vocabulary trainer. For Germans to learn e.g. English.

## Build and run locally

* setup docker
* `mvn clean package`
* `./run.sh`
* point your browser to `http://localhost:8080` and login with the credentials printed on the command line
* stop with ctrl-c


## Directory structure for running application

/usr/local/yogi/
      - etc
         - books                   available books
         - user.properties
      - run                        cwd for Tomcat and "server.tomcat.basedir"
         - logs                    Tomcat Access Logs
         - protocols               Übungsprotokolle ("userProtocols")
           - <book>
             - freigeschaltet.selection
             - *.selection         Arbitrary Selection
             - 1.protocol          Übung 1
             - 2.protocol          Übung 2
             - ...
         - work                    Tomcat "work" directory

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

to create a systemd service on a Linux box

* install docker and docker-compose
* `scp -r src/systemd yourhost:~/yogi/`
* on *yourhost*
  * `mkdir ~/yogi/run`
  * setup `~/yogi/etc` with a ´books` subdirectory and your books
  * `ln -s ~/yogi/systemd/yogi.service /etc/systemd/system
  * `systemctl daemon-reload`
  * `systemctl start yogi`
  * `systemctl enable yogi`


# Yogi

Vokabel Trainer.

Setup mit

alias yogi="docker-compose -f /Users/mhm/Projects/github.com/net/mlhartme/yogi/docker-compose.yaml"
alias public-yogi="DOCKER_HOST=mops:2375 docker-compose -f /Users/mhm/Projects/github.com/net/mlhartme/yogi/mops-compose.yaml"

    
Satzzeichen, Groß/Kleinschreibung müssen exakt eingegeben werden. Wenn die Frage mit Satzanfang/Groß beginnt und einem Satzzeichen endet,
dann soll das die Antowort auch machen.

## Server aufsetzen

Um Yogi als Systemd Service auf einem Linux Rechner aufzusetzen:

* docker und docker-compose installieren
* `scp -r src/systemd yourhost:~/yogi/`
* auf *yourhost*
  * `mkdir ~/yogi/run`
  * ~/yogi/etc einrichten
  * `ln -s ~/yogi/systemd/yogi.service /etc/systemd/system
  * `systemctl daemon-reload`
  * `systemctl start yogi`
  * `systemctl enable yogi`

## Bauen und lokal starten

* mvn clean package
* ./run.sh
* http://localhost:8080

## Directory Aufbau

/usr/local/yogi/
      - etc
         - books                   Verfügbare Bücher
      - run                        Aktuelles Verzeichnis für Tomcat Prozess. Und "server.tomcat.basedir"
         - logs                    Tomcat Access Logs
         - protocols               Übungsprotokolle
         - work                    Tomcat "work" directory

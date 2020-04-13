# Yogi

Vokabel Trainer.

Setup mit

alias yogi="docker-compose -f /Users/mhm/Projects/github.com/net/mlhartme/yogi/docker-compose.yaml"
alias public-yogi="DOCKER_HOST=mops:2375 docker-compose -f /Users/mhm/Projects/github.com/net/mlhartme/yogi/mops-compose.yaml"

    
Satzzeichen, Groß/Kleinschreibung müssen exakt eingegeben werden. Wenn die Frage mit Satzanfang/Groß beginnt und einem Satzzeichen endet,
dann soll das die Antowort auch machen.

## Setup on mops

scp -r src/systemd mops:~/yogi/
install docker-compose
symlink service into /etc/systemd/system
systemctl daemon-reload
systemctl start yogi
systemctl enable yogi

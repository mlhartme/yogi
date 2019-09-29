# Yogi

Vokabel Trainer.

Starten mit

    mvn clean package
    java -jar -Dserver.tomcat.basedir=tomcat -Dserver.tomcat.accesslog.enabled=true target/yogi-*.jar
    
    
Satzzeichen, Groß/Kleinschreibung müssen exakt eingegeben werden. Wenn die Frage mit Satzanfang/Groß beginnt und einem Satzzeichen endet,
dann soll das die Antowort auch machen.
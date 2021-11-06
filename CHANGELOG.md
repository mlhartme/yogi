# Changelog

## 1.4.0 (pending)

* new ui
  * new books page
  * new navigation
  * unified selections, with multi-select
  * favicon
* implementation tweaks
  * renamed package net.mlhartme to de.schmizzolin. Reserved de.schmizzolin on Maven central
  * added application properties: yogi.lib, yogi.etc
  * replaced publish.sh and src/systemd by kubernetes deployments in src/kubernetes
  * renamed *.log to *.protocol
  * renamed .enabled to freigeschaltet.selection
  * update parent 1.5.0 to 1.5.1
  * update spring-boot 2.4.4 to 2.5.6
  * update bootstrap 4.6.0 to 5.1.1
  * update JavaScript code to work without jQuery


## 1.3.0 (2021-09-03)

* improved protocol overview with date, count and improvements
* build for Java 16
* update dependencies: spring, springboot, bootstrap 4.5.2 to 4.6.0, jquery 3.5.1 to 3.6.0, junit 5.7.0 to 5.7.1


## 1.2.0 (2021-04-02)

* update dependencies: spring, springboot, thymeleaf, docker plugin, bootstrap (with popper and jquery)
* ui 
  * add version to footer
* added `run.sh` to run locally
* lazy-foss-parent, build for Java 11
* /usr/local/yogi/etc
  * load users from /usr/local/yogi/etc/user.properties
  * load books from /usr/local/yogi/etc/books


## 1.1.0 (2021-01-08)

* moved `Üben` button up on book level, letting the computer pick vocabulary across sections as needed;
* renamed `Auswahl` button on selection page to `Üben ...`
* changed color for 0% to red
* improved quality computation: when asked only once or twice, the missing answers to three are considered "not answered"
* added multi user support
* select excercise count, scope or selection

* renamed `logs` directory to `protocols`
* fixed protocol sorting
* fixed IO exception when logs directory is wiped.
* fixed divide by 0 for empty protocols

* update spring-boot 2.1.2 to 2.2.6
* update to sushi 3.3.0


## 1.0.0 (2019-09-09)

Initial tag. Jakob repeated his first year with it.

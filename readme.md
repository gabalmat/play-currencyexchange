# NCSU CSC 750 (Service Oriented Computing)

Project 2

Play framework server application integrated with Akka to implement actor model.

The system has two roles:
UserActor and MarketActor. For this prototype, users can trade in only one currency, Bitcoin
(BTC). Actors communicate with "outside world" via RESTful API. 

Ingredients:
- JDK 8 or higher
- sbt (ver 1.2.1 or higher)
- Play (ver 2.6.18 or higher)
- Akka (ver 2.5.4 or higher)
- sqlite-jdbc (ver 3.23.1 or higher)
- May need to add the following to build.sbt:
- libraryDependencies += "org.xerial" % "sqlite-jdbc" % "3.8.11.2"
- Jackson JSON library

For more details including RESTful API specs, see [Assignment Instructions](doc/Project2_assignment.pdf)


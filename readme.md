# NCSU CSC 750 (Service Oriented Computing)

Project 2

Play framework server RESTful application integrated with Akka to implement actor model.

The system has two roles:
UserActor and MarketActor . For our prototype, users can trade in only one currency, Bitcoin
(BTC).

Ingredients:
- JDK 8 or higher
- sbt (ver 1.2.1 or higher)
- Play (ver 2.6.18 or higher) [Don’t need to download manually]
- Akka (ver 2.5.4 or higher)
- sqlite-jdbc (ver 3.23.1 or higher)
- May need to add the following to build.sbt:
- libraryDependencies += "org.xerial" % "sqlite-jdbc" % "3.8.11.2"
- Jackson JSON library


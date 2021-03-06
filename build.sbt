name := "services"
organization := "com.affaprop.weibs"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.3"

libraryDependencies += guice
libraryDependencies += "com.typesafe.play" %% "play-slick" % "3.0.0" withSources() withJavadoc()
libraryDependencies += "org.postgresql" % "postgresql" % "42.2.1" withSources() withJavadoc()
libraryDependencies ++= Seq(evolutions, jdbc)
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test withSources() withJavadoc()
libraryDependencies += "com.h2database" % "h2" % "1.4.196" % Test

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.affaprop.weibs.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.affaprop.weibs.binders._"

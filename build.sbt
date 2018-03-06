name := """service"""
organization := "com.affaprop.weibs"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.3"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test withSources() withJavadoc()
libraryDependencies += "com.typesafe.play" %% "play-slick" % "3.0.0" withSources() withJavadoc()
libraryDependencies += "org.postgresql" % "postgresql" % "42.2.1" withSources() withJavadoc()
libraryDependencies += evolutions
// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.affaprop.weibs.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.affaprop.weibs.binders._"

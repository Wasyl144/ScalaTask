scalaVersion := "2.13.3"

name := "hello-world"
organization := "ch.epfl.scala"
version := "1.0"

val AkkaVersion = "2.6.8"
val AkkaHttpVersion = "10.2.4"
val circeVersion = "0.13.0"
val slf4jVersion = "1.7.5"
val scalaTestVersion = "3.2.0"

libraryDependencies ++= Seq(
    "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2",
    "org.scala-lang.modules" %% "scala-xml" % "1.3.0",
    "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
    "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
    "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
    "io.circe" %% "circe-core" % circeVersion,
    "io.circe" %% "circe-parser" % circeVersion,
    "io.circe" %% "circe-optics" % circeVersion,
    "io.circe" %% "circe-generic" % circeVersion,
    "com.lihaoyi" %% "os-lib" % "0.7.3",
    "edu.stanford.nlp" % "stanford-corenlp" % "4.0.0",
    "edu.stanford.nlp" % "stanford-corenlp" % "4.0.0" classifier("models"),
    "org.slf4j" % "slf4j-api" % slf4jVersion,
    "org.slf4j" % "slf4j-simple" % slf4jVersion,
    "org.scalatest" %% "scalatest" % scalaTestVersion,
    "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
)

javaOptions += "-Xmx5G"

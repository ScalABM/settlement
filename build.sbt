// define some common build settings used by core auctions API as well as the various testing configurations
lazy val commonSettings = Seq(
  scalaVersion := "2.12.2" ,
  name := "esl-settlement",
  version := "0.1.0-SNAPSHOT",
  organization := "org.economicsl",
  organizationName := "EconomicSL",
  organizationHomepage := Some(url("https://economicsl.github.io/")),
  resolvers ++= Seq(
    "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
    "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases"
  ),
  libraryDependencies ++= Seq(
    "com.typesafe" % "config" % "1.3.1",
    "com.typesafe.akka" %% "akka-actor" % "2.5.6",
    "com.typesafe.akka" %% "akka-testkit" % "2.5.6" % "test",
    "org.economicsl" %% "esl-core" % "0.1.0-SNAPSHOT",
    "org.scalatest" %% "scalatest" % "3.0.1" % "test"
  ),
  scalacOptions ++= Seq(
    "-deprecation",  // issue warning if we use any deprecated API features
    "-feature",  // tells the compiler to provide information about misused language features
    "-Xlint",
    "-Ywarn-unused-import",
    "-Ywarn-dead-code"
  )
)

// finally define the full project build settings
lazy val core = (project in file(".")).settings(commonSettings: _*)

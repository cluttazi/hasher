lazy val pekkoVersion = "1.6.0"
lazy val pekkoHttpVersion = "1.3.0"

lazy val root = (project in file(".")).settings(
  inThisBuild(
    List(
      organization := "com.chrisluttazi",
      scalaVersion := "2.13.18"
    )
  ),
  name := "hasher",
  scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlint"),
  libraryDependencies ++= Seq(
    "org.apache.pekko" %% "pekko-http" % pekkoHttpVersion,
    "org.apache.pekko" %% "pekko-http-spray-json" % pekkoHttpVersion,
    "org.apache.pekko" %% "pekko-stream" % pekkoVersion,
    "org.apache.pekko" %% "pekko-http-testkit" % pekkoHttpVersion % Test,
    "org.apache.pekko" %% "pekko-testkit" % pekkoVersion % Test,
    "org.apache.pekko" %% "pekko-stream-testkit" % pekkoVersion % Test,
    "org.scalatest" %% "scalatest" % "3.2.20" % Test
  )
)

name := "SIQ demo"

version := "1.0"

scalaVersion := "2.9.1"

libraryDependencies ++= Seq(
  "junit" % "junit" % "4.8" % "test",
  "org.scalatest" % "scalatest_2.9.0" % "1.6.1" % "test",
  "org.squeryl" %% "squeryl" % "0.9.4",
  "org.scalaquery" % "scalaquery_2.9.0-1" % "0.9.5",
  "org.apache.derby" % "derby" % "10.8.1.2"
)


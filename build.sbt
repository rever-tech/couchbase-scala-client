organization := "rever.client4s"

name := "couchbase-scala-client"

version := "0.1-SNAPSHOT"

scalaVersion := "2.11.8"

libraryDependencies += "com.couchbase.client" % "core-io" % "1.4.2"
libraryDependencies += "com.couchbase.client" % "java-client" % "2.4.2"
libraryDependencies += "io.reactivex" %% "rxscala" % "0.25.0"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0" % "test"

resolvers += "Artifactory" at "http://central.rever.vn/artifactory/libs-release-local/"

publishTo := Some("Artifactory Realm" at "http://central.rever.vn/artifactory/libs-release-local")

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
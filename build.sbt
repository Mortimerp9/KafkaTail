scalaVersion := "2.9.3"

organization := "com.example"

name := "KafkaTail"

version := "0.1.0"

libraryDependencies ++= Seq(
  "net.databinder" %% "unfiltered-netty-websockets" % "0.7.0",
   "org.apache.kafka" % "kafka_2.9.1" % "0.7.0",
    "zkclient" % "zkclient" % "0.2.dev",
    "org.apache.zookeeper" % "zookeeper" % "3.3.2",
    "net.sf.jopt-simple" % "jopt-simple" % "3.2",
    "org.slf4j" % "slf4j-simple" % "1.6.4",
    "com.yammer.metrics" % "metrics-core" % "2.2.0",
    "com.yammer.metrics" % "metrics-annotation" % "2.2.0",
    "log4j" % "log4j" % "1.2.15"
).map(_.excludeAll(
      ExclusionRule(organization = "com.sun.jdmk"),
      ExclusionRule(organization = "com.sun.jmx"),
      ExclusionRule(organization = "javax.jms")
    )
 )


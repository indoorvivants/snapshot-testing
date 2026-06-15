addSbtPlugin("com.github.sbt" % "sbt-ci-release"    % "1.11.2")
addSbtPlugin("com.eed3si9n"   % "sbt-projectmatrix" % "0.11.0")

// Code quality
//addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat"    % "0.4.2")
addSbtPlugin("org.scalameta"     % "sbt-scalafmt"  % "2.5.6")
addSbtPlugin("ch.epfl.scala"     % "sbt-scalafix"  % "0.14.6")
addSbtPlugin("com.eed3si9n"      % "sbt-buildinfo" % "0.11.0")
addSbtPlugin("de.heikoseeberger" % "sbt-header"    % "5.10.0")

// Compiled documentation
addSbtPlugin("org.scalameta" % "sbt-mdoc" % "2.5.1")

// Scala.js and Scala Native
addSbtPlugin("org.scala-js"     % "sbt-scalajs"      % "1.21.0")
addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.5.11")

libraryDependencies ++= List(
  "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value
)
Compile / unmanagedSourceDirectories +=
  (ThisBuild / baseDirectory).value.getParentFile /
    "modules" / "snapshots-sbt-plugin" / "src" / "main" / "scala"

Compile / unmanagedSourceDirectories +=
  (ThisBuild / baseDirectory).value.getParentFile /
    "modules" / "snapshots-sbt-plugin" / "src" / "main" / "scala-2"

Compile / unmanagedSourceDirectories +=
  (ThisBuild / baseDirectory).value.getParentFile /
    "modules" / "snapshots-buildtime" / "src" / "main" / "scala"

Compile / unmanagedResourceDirectories +=
  (ThisBuild / baseDirectory).value.getParentFile /
    "modules" / "snapshots-buildtime" / "src" / "main" / "resources"

Compile / sourceGenerators += Def.task {
  val tmpDest =
    (Compile / managedResourceDirectories).value.head / "BuildInfo.scala"

  IO.write(
    tmpDest,
    "package com.indoorvivants.snapshots.sbtplugin\nobject BuildInfo {def version: String = \"dev\"}"
  )

  Seq(tmpDest)
}

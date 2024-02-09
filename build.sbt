Global / excludeLintKeys += logManager
Global / excludeLintKeys += scalaJSUseMainModuleInitializer
Global / excludeLintKeys += scalaJSLinkerConfig

inThisBuild(
  List(
    semanticdbEnabled          := true,
    semanticdbVersion          := scalafixSemanticdb.revision,
    scalafixScalaBinaryVersion := scalaBinaryVersion.value,
    organization               := "com.indoorvivants.snapshots",
    organizationName           := "Anton Sviridov",
    homepage := Some(
      url("https://github.com/indoorvivants/snapshot-testing")
    ),
    startYear := Some(2024),
    licenses := List(
      "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")
    ),
    developers := List(
      Developer(
        "keynmol",
        "Anton Sviridov",
        "keynmol@gmail.com",
        url("https://blog.indoorvivants.com")
      )
    )
  )
)

val Versions = new {
  val Scala213      = "2.13.12"
  val Scala212      = "2.12.16"
  val Scala3        = "3.3.1"
  val scalaVersions = Seq(Scala3, Scala212, Scala213)
  val munit         = "1.0.0-M7"
}

lazy val root = project
  .in(file("."))
  .aggregate(snapshotsRuntime.projectRefs*)
  .aggregate(snapshotsBuildtime.projectRefs*)
  .aggregate(snapshotsSbtPlugin.projectRefs*)
  .settings(noPublish)

lazy val snapshotsRuntime = projectMatrix
  .in(file("modules/snapshots-runtime"))
  .defaultAxes(defaults*)
  .settings(
    name := "snapshots-runtime"
  )
  .settings(munitSettings)
  .jvmPlatform(Versions.scalaVersions)
  .jsPlatform(Versions.scalaVersions, disableDependencyChecks)
  .nativePlatform(Versions.scalaVersions, disableDependencyChecks)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule)),
    nativeConfig ~= (_.withIncrementalCompilation(true))
  )

lazy val snapshotsBuildtime = projectMatrix
  .in(file("modules/snapshots-buildtime"))
  .settings(
    name := "snapshots-buildtime"
  )
  .settings(munitSettings)
  .jvmPlatform(Versions.scalaVersions)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule)),
    nativeConfig ~= (_.withIncrementalCompilation(true))
  )

lazy val snapshotsSbtPlugin = projectMatrix
  .dependsOn(snapshotsBuildtime)
  .jvmPlatform(Seq(Versions.Scala212))
  .in(file("modules/snapshots-sbt-plugin"))
  .settings(
    sbtPlugin := true,
    name      := "sbt-snapshots"
  )
  .enablePlugins(BuildInfoPlugin)
  .settings(
    buildInfoPackage := "com.indoorvivants.snapshots.sbtplugin",
    buildInfoKeys := Seq[BuildInfoKey](
      version,
      scalaVersion,
      scalaBinaryVersion
    )
  )

/*
lazy val docs = projectMatrix
  .in(file("myproject-docs"))
  .dependsOn(core)
  .defaultAxes(defaults*)
  .settings(
    mdocVariables := Map(
      "VERSION" -> version.value
    )
  )
  .settings(disableDependencyChecks)
  .jvmPlatform(Versions.scalaVersions)
  .enablePlugins(MdocPlugin)
  .settings(noPublish)
 */

val noPublish = Seq(
  publish / skip      := true,
  publishLocal / skip := true
)

val defaults =
  Seq(VirtualAxis.scalaABIVersion(Versions.Scala3), VirtualAxis.jvm)

val scalafixRules = Seq(
  "OrganizeImports",
  "DisableSyntax",
  "LeakingImplicitClassVal",
  "NoValInForComprehension"
).mkString(" ")

val CICommands = Seq(
  "clean",
  "compile",
  "test",
  "docs/mdoc",
  "scalafmtCheckAll",
  "scalafmtSbtCheck",
  s"scalafix --check $scalafixRules",
  "headerCheck",
  "undeclaredCompileDependenciesTest",
  "unusedCompileDependenciesTest",
  "missinglinkCheck"
).mkString(";")

val PrepareCICommands = Seq(
  s"scalafix --rules $scalafixRules",
  "scalafmtAll",
  "scalafmtSbt",
  "headerCreate",
  "undeclaredCompileDependenciesTest"
).mkString(";")

addCommandAlias("ci", CICommands)

addCommandAlias("preCI", PrepareCICommands)

Global / onChangedBuildSource := ReloadOnSourceChanges

// https://github.com/cb372/sbt-explicit-dependencies/issues/27
lazy val disableDependencyChecks = Seq(
  unusedCompileDependenciesTest     := {},
  missinglinkCheck                  := {},
  undeclaredCompileDependenciesTest := {}
)

lazy val munitSettings = Seq(
  libraryDependencies += {
    "org.scalameta" %%% "munit" % Versions.munit % Test
  }
)

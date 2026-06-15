Global / excludeLintKeys += logManager
Global / excludeLintKeys += scalaJSUseMainModuleInitializer
Global / excludeLintKeys += scalaJSLinkerConfig

inThisBuild(
  List(
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    organization      := "com.indoorvivants.snapshots",
    organizationName  := "Anton Sviridov",
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

organization := "com.indoorvivants.snapshots"

val Versions = new {
  val Scala213      = "2.13.17"
  val Scala212      = "2.12.21"
  val Scala3        = "3.3.7"
  val Scala3Next    = "3.8.4"
  val scalaVersions = Seq(Scala3, Scala212, Scala213)
  val scala3Next    = Seq(Scala3Next)
  val munit         = "1.3.0"
  val upickle       = "4.4.3"

  val Sbt1      = "1.12.12"
  val Sbt2      = "2.0.0"
  val Sbt1Scala = Scala212
  val Sbt2Scala = "3.8.4"
}

lazy val root: Project = project
  .in(file("."))
  .aggregate(snapshotsRuntime.projectRefs*)
  .aggregate(snapshotsBuildtime.projectRefs*)
  .aggregate(snapshotsSbtPlugin.projectRefs*)
  .aggregate(example.projectRefs*)
  .aggregate(exampleScalaNext)
  .settings(noPublish)

lazy val snapshotsRuntime = projectMatrix
  .in(file("modules/snapshots-runtime"))
  .defaultAxes(defaults*)
  .settings(
    name := "snapshots-runtime"
  )
  .settings(munitSettings)
  .jvmPlatform(Versions.scalaVersions)
  .jsPlatform(Versions.scalaVersions)
  .nativePlatform(Versions.scalaVersions)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule)),
    nativeConfig ~= (_.withIncrementalCompilation(true))
  )

lazy val exampleSettings: Seq[Def.Setting[_]] =
  Seq(
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule)),
    nativeConfig ~= (_.withIncrementalCompilation(true)),
    snapshotsPackageName          := "example",
    snapshotsAddRuntimeDependency := false,
    snapshotsIntegrations += SnapshotIntegration.MUnit,
    libraryDependencies += "com.lihaoyi" %%% "upickle" % Versions.upickle,
    scalacOptions ++= Seq("-deprecation"),
    scalacOptions ++= {
      if (scalaVersion.value.startsWith("2."))
        Seq("-Ywarn-unused", "-Xfatal-warnings")
      else Seq("-Wunused:all", "-Werror")
    }
  ) ++ noPublish

lazy val example = projectMatrix
  .dependsOn(snapshotsRuntime)
  .in(file("modules/example"))
  .defaultAxes(defaults*)
  .settings(munitSettings)
  .jvmPlatform(Versions.scalaVersions)
  .jsPlatform(Versions.scalaVersions)
  .nativePlatform(Versions.scalaVersions)
  .settings(exampleSettings)
  .enablePlugins(SnapshotsPlugin)

lazy val exampleScalaNext = project
  .dependsOn(snapshotsRuntime.jvm(Versions.Scala3))
  .in(file("modules/example"))
  .settings(munitSettings)
  .settings(target := target.value / "next")
  .settings(
    snapshotsPackageName          := "example",
    snapshotsAddRuntimeDependency := false,
    snapshotsIntegrations += SnapshotIntegration.MUnit,
    libraryDependencies += "com.lihaoyi" %%% "upickle" % Versions.upickle,
    scalacOptions ++= Seq("-Wunused:all"),
    scalaVersion := Versions.Scala3Next,
    noPublish
  )
  .enablePlugins(SnapshotsPlugin)

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
  .enablePlugins(ScriptedPlugin, SbtPlugin)
  .jvmPlatform(Seq(Versions.Sbt1Scala, Versions.Sbt2Scala))
  .in(file("modules/snapshots-sbt-plugin"))
  .settings(
    (pluginCrossBuild / sbtVersion) := {
      scalaBinaryVersion.value match {
        case "2.12" => Versions.Sbt1
        case _      => Versions.Sbt2
      }
    },
    scalacOptions ++= {
      scalaBinaryVersion.value match {
        case "2.12" => "-Xsource:3" :: Nil
        case _      => Nil
      }
    },
    sbtTestDirectory := {
      scalaBinaryVersion.value match {
        case "2.12" => (sourceDirectory).value / "sbt-test"
        case _      => (sourceDirectory).value / "sbt-test-sbt2"
      }
    },
    scriptedLaunchOpts := {
      scriptedLaunchOpts.value ++
        Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
    },
    scriptedBufferLog := false
  )
  .settings(
    sbtPlugin := true,
    name      := "sbt-snapshots",
    publishLocal := publishLocal
      .dependsOn(
        snapshotsBuildtime.jvm(Versions.Scala212) / publishLocal,
        snapshotsRuntime.js(Versions.Scala3) / publishLocal,
        snapshotsRuntime.native(Versions.Scala3) / publishLocal,
        snapshotsRuntime.jvm(Versions.Scala3) / publishLocal,
        snapshotsRuntime.jvm(Versions.Scala213) / publishLocal
      )
      .value
  )
  .enablePlugins(ScriptedPlugin, SbtPlugin)
  .enablePlugins(BuildInfoPlugin)
  .settings(
    buildInfoPackage := "com.indoorvivants.snapshots.sbtplugin",
    buildInfoKeys := Seq[BuildInfoKey](
      version,
      scalaVersion,
      scalaBinaryVersion
    )
  )

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
  "scalafixEnable",
  "compile",
  "test",
  "example/test",
  "scripted",
  "scalafmtCheckAll",
  "scalafmtSbtCheck",
  s"scalafix --check $scalafixRules",
  "headerCheck"
).mkString(";")

val PrepareCICommands = Seq(
  "scalafixEnable",
  s"scalafix --rules $scalafixRules",
  "scalafmtAll",
  "scalafmtSbt",
  "headerCreate"
).mkString(";")

addCommandAlias("ci", CICommands)

addCommandAlias("preCI", PrepareCICommands)

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val munitSettings = Seq(
  libraryDependencies += {
    "org.scalameta" %%% "munit" % Versions.munit % Test
  }
)

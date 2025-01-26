Global / excludeLintKeys += logManager
Global / excludeLintKeys += scalaJSUseMainModuleInitializer
Global / excludeLintKeys += scalaJSLinkerConfig

inThisBuild(
  List(
    semanticdbEnabled   := true,
    semanticdbVersion   := scalafixSemanticdb.revision,
    organization        := "com.indoorvivants.snapshots",
    sonatypeProfileName := "com.indoorvivants",
    organizationName    := "Anton Sviridov",
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

organization        := "com.indoorvivants.snapshots"
sonatypeProfileName := "com.indoorvivants"

val Versions = new {
  val Scala213      = "2.13.16"
  val Scala212      = "2.12.20"
  val Scala3        = "3.3.4"
  val Scala3Next    = "3.6.3"
  val scalaVersions = Seq(Scala3, Scala212, Scala213)
  val scala3Next    = Seq(Scala3Next)
  val munit         = "1.1.0"
  val upickle       = "3.3.1"
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
    scalacOptions ++= Seq("-Xfatal-warnings", "-deprecation"),
    scalacOptions += {
      if (scalaVersion.value.startsWith("2.")) "-Ywarn-unused"
      else "-Wunused:all"
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
    libraryDependencies += "com.lihaoyi" %%% "upickle" % "3.3.1",
    scalacOptions ++= Seq("-Wunused:all", "-Xfatal-warnings"),
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
  .jvmPlatform(Seq(Versions.Scala212))
  .in(file("modules/snapshots-sbt-plugin"))
  .settings(
    sbtPlugin := true,
    name      := "sbt-snapshots",
    scriptedLaunchOpts := {
      scriptedLaunchOpts.value ++
        Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
    },
    scriptedBufferLog := false,
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

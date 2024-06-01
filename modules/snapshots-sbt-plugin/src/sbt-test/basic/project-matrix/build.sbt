lazy val root = projectMatrix
  .in(file("."))
  .jvmPlatform(Seq("3.3.1", "2.13.14"))
  .nativePlatform(Seq("3.3.1"))
  .jsPlatform(Seq("3.3.1"))
  .settings(
    snapshotsPackageName := "example.bla",
    snapshotsIntegrations += SnapshotIntegration.MUnit,
    libraryDependencies +=
      "org.scalameta" %%% "munit" % "1.0.0" % Test,
    scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule)),
  )
  .enablePlugins(SnapshotsPlugin)


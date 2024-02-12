lazy val root = projectMatrix
  .in(file("."))
  .jvmPlatform(Seq("3.3.1", "2.13.12"))
  .nativePlatform(Seq("3.3.1"))
  .jsPlatform(Seq("3.3.1"))
  .settings(
    snapshotsPackageName := "example.bla",
    snapshotsIntegrations += SnapshotIntegration.MUnit,
    libraryDependencies +=
      "org.scalameta" %%% "munit" % "1.0.0-M7" % Test,
    scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule)),
  )
  .enablePlugins(SnapshotsPlugin)


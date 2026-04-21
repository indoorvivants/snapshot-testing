lazy val root = projectMatrix
  .in(file("."))
  .jvmPlatform(Seq("3.3.7", "2.13.17"))
  .nativePlatform(Seq("3.3.7"))
  .jsPlatform(Seq("3.3.7"))
  .settings(
    snapshotsPackageName := "example.bla",
    snapshotsIntegrations += SnapshotIntegration.MUnit,
    libraryDependencies +=
      "org.scalameta" %%% "munit" % "1.1.0" % Test,
    scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule)),
    snapshotsForceOverwrite := true,
  )
  .enablePlugins(SnapshotsPlugin)


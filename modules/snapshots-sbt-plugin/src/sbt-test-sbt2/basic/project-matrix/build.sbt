lazy val root = projectMatrix
  .in(file("."))
  .jvmPlatform(Seq("3.3.7", "2.13.17"))
  .nativePlatform(Seq("3.3.7"))
  // .jsPlatform(Seq("3.3.7"))
  .settings(
    snapshotsPackageName := "example.bla",
    snapshotsIntegrations += SnapshotIntegration.MUnit,
    snapshotsForceOverwrite := false,
    libraryDependencies +=
      "org.scalameta" %% "munit" % "1.3.3" % Test,
    // scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule)),
  )
  .enablePlugins(SnapshotsPlugin)


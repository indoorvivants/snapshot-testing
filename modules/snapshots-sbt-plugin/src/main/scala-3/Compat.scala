package com.indoorvivants.snapshots.sbtplugin

import sbt.*, Keys.*

private[sbtplugin] object Compat:
  import SnapshotsPlugin.autoImport.snapshotsAddRuntimeDependency
  val addRuntimeDep = Seq(
    libraryDependencies ++= {
      if snapshotsAddRuntimeDependency.value then
        val cross = platform.value match
          case "jvm" => scalaBinaryVersion.value
          case other => other + "_" + scalaBinaryVersion.value

        Seq(
          "com.indoorvivants.snapshots" % s"snapshots-runtime_$cross" % BuildInfo.version % Test
        )
      else Seq.empty
    }
  )
end Compat

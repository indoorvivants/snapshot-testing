package com.indoorvivants.snapshots.sbtplugin

import sbt.*, Keys.*

private[sbtplugin] object Compat {
  import SnapshotsPlugin.autoImport.snapshotsAddRuntimeDependency
  val addRuntimeDep = Seq(
    libraryDependencies ++= {
      if (snapshotsAddRuntimeDependency.value) {
        val cross = crossVersion.value match {
          case b: Binary => b.prefix + scalaBinaryVersion.value
          case _         => scalaBinaryVersion.value
        }

        Seq(
          "com.indoorvivants.snapshots" % s"snapshots-runtime_$cross" % BuildInfo.version % Test
        )
      } else Seq.empty
    }
  )
}

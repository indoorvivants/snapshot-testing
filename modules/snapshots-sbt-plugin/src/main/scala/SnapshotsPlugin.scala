package com.indoorvivants.snapshots.sbtplugin

import sbt.Keys.*
import sbt.nio.Keys.*
import sbt.*
import scala.io.StdIn
import com.indoorvivants.snapshots.build.SnapshotsBuild

object SnapshotsPlugin extends AutoPlugin {
  object autoImport {
    val snapshotsProjectIdentifier    = settingKey[String]("")
    val snapshotsPackageName          = settingKey[String]("")
    val snapshotsAddRuntimeDependency = settingKey[Boolean]("")
    val tag            = ConcurrentRestrictions.Tag("snapshots-check")
    val snapshotsCheck = taskKey[Unit]("")
  }

  import autoImport.*

  override def globalSettings: Seq[Setting[?]] = Seq(
    concurrentRestrictions += Tags.limit(tag, 1)
  )

  override def projectSettings: Seq[Setting[?]] =
    Seq(
      libraryDependencies ++= {
        if (snapshotsAddRuntimeDependency.value) {
          val cross = crossVersion.value match {
            case b: Binary => b.prefix + scalaBinaryVersion.value
            case _         => scalaBinaryVersion.value
          }

          Seq(
            "tech.neander" % s"snapshots-runtime_$cross" % BuildInfo.version
          )
        } else Seq.empty
      },
      snapshotsProjectIdentifier    := moduleName.value,
      snapshotsAddRuntimeDependency := true,
      snapshotsCheck := Def
        .task {
          SnapshotsBuild.checkSnapshots(
            (Test / managedResourceDirectories).value.head / "snapshots-tmp",
            snapshotsProjectIdentifier.value
          )
        }
        .tag(tag)
        .value,
      Test / sourceGenerators += Def.task {
        SnapshotsBuild.generateSources(
          projectId = snapshotsProjectIdentifier.value,
          packageName = snapshotsPackageName.value,
          snapshotsDestination =
            (Test / resourceDirectory).value / "snapshots" / snapshotsProjectIdentifier.value,
          sourceDestination =
            (Test / managedSourceDirectories).value.head / "Snapshots.scala",
          tmpLocation =
            (Test / managedResourceDirectories).value.head / "snapshots-tmp"
        )
      }
    )

  def SnapshotsGenerate(path: File, tempPath: File, packageName: String) =
    s"""
     |package $packageName
     |object Snapshots extends proompts.snapshots.Snapshots(location = "$path", tmpLocation = "$tempPath")
      """.trim.stripMargin

}

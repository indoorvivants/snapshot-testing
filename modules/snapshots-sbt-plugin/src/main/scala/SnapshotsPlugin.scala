/*
 * Copyright 2024 Anton Sviridov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.indoorvivants.snapshots.sbtplugin

import scala.io.StdIn

import com.indoorvivants.snapshots.build.SnapshotsBuild
import sbt.Keys.*
import sbt.*
import sbt.nio.Keys.*

import SnapshotsBuild.SnapshotAction

object SnapshotsPlugin extends AutoPlugin {
  object autoImport {
    val snapshotsProjectIdentifier = settingKey[String](
      "Project identifier to separate snapshots coming from cross-platform projects"
    )
    val snapshotsPackageName = settingKey[String](
      "Package name under which the generated Snapshots object will be created"
    )
    val snapshotsAddRuntimeDependency = settingKey[Boolean](
      "Whether to add snapshot runtime to the build - true by default, you shouldn't need to touch this"
    )
    val snapshotsTemporaryDirectory =
      settingKey[File]("Temp folder where snapshot diffs will be created")
    val snapshotsCheck =
      taskKey[Unit]("Interactively accept modified snapshotsAcceptAll")
    val snapshotsAcceptAll = taskKey[Unit]("Accept all modified snapshots")
    val snapshotsDiscardAll =
      taskKey[Unit]("Discard all modifications to snapshots")
  }

  val snapshotsTag = ConcurrentRestrictions.Tag("snapshots-check")

  import autoImport.*

  override def globalSettings: Seq[Setting[?]] = Seq(
    concurrentRestrictions += Tags.limit(snapshotsTag, 1)
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
      snapshotsProjectIdentifier    := thisProject.value.id,
      snapshotsAddRuntimeDependency := true,
      snapshotsTemporaryDirectory := (Test / managedResourceDirectories).value.head / "snapshots-tmp",
      snapshotsCheck := Def
        .task {
          SnapshotsBuild.checkSnapshots(
            snapshotsTemporaryDirectory.value,
            snapshotsProjectIdentifier.value,
            SnapshotAction.Interactive
          )
        }
        .tag(snapshotsTag)
        .value,
      snapshotsAcceptAll := Def
        .task {
          SnapshotsBuild.checkSnapshots(
            snapshotsTemporaryDirectory.value,
            snapshotsProjectIdentifier.value,
            SnapshotAction.Accept
          )
        }
        .tag(snapshotsTag)
        .value,
      snapshotsDiscardAll := Def
        .task {
          SnapshotsBuild.checkSnapshots(
            snapshotsTemporaryDirectory.value,
            snapshotsProjectIdentifier.value,
            SnapshotAction.Discard
          )
        }
        .tag(snapshotsTag)
        .value,
      Test / sourceGenerators += Def.task {
        SnapshotsBuild.generateSources(
          projectId = snapshotsProjectIdentifier.value,
          packageName = snapshotsPackageName.value,
          snapshotsDestination =
            (Test / resourceDirectory).value / "snapshots" / snapshotsProjectIdentifier.value,
          sourceDestination =
            (Test / managedSourceDirectories).value.head / "Snapshots.scala",
          tmpLocation = snapshotsTemporaryDirectory.value
        )
      }
    )
}

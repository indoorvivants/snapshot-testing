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
import sbt.Keys._
import sbt._
import sbt.nio.Keys._

import SnapshotsBuild.{SnapshotAction, SnapshotIntegration}

object SnapshotsPlugin extends AutoPlugin {
  object autoImport {
    val snapshotsProjectIdentifier = settingKey[String](
      "Project identifier to separate snapshots coming from cross-platform projects"
    )

    val snapshotsPackageName = settingKey[String](
      "Package name under which the generated Snapshots object will be created"
    )

    val snapshotsIntegrations = settingKey[Seq[SnapshotIntegration]](
      "Test framework integrations to generate in test sources"
    )

    val snapshotsAddRuntimeDependency = settingKey[Boolean](
      "Whether to add snapshot runtime to the build - true by default, you shouldn't need to touch this"
    )

    val snapshotsForceOverwrite = settingKey[Boolean](
      "(default: false) If set to true, tests where there is a snapshot mismatch won't fail, instead overwriting snapshot directly with new contents - meaning you don't have to run snapshotCheck/snapshotsAcceptAll.\n" +
        "A recommended workflow with this option is to only enable it if NOT running on CI - e.g. `snapshotsForceOverwrite := !sys.env.contains(\"CI\")`.\n" +
        "This way snapshot compliance will be checked on CI, but local workflow will be much quicker with immediate snapshot overwriting"
    )

    val snapshotsLocation = settingKey[File](
      "Location of the snapshot files (this location is checked in to your VCS)"
    )

    val snapshotsTemporaryDirectory =
      settingKey[File]("Temp folder where snapshot diffs will be created")

    val snapshotsCheck =
      taskKey[Unit]("Interactively accept modified snapshotsAcceptAll")

    val snapshotsAcceptAll = taskKey[Unit]("Accept all modified snapshots")

    val snapshotsDiscardAll =
      taskKey[Unit]("Discard all modifications to snapshots")

    val snapshotsMigrate = inputKey[Unit](
      "Apply a known migration to snapshot files. This task aids in upgrading the plugin version if there are breaking changes"
    )

    val SnapshotIntegration = SnapshotsBuild.SnapshotIntegration
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
            "com.indoorvivants.snapshots" % s"snapshots-runtime_$cross" % BuildInfo.version % Test
          )
        } else Seq.empty
      },
      snapshotsProjectIdentifier    := thisProject.value.id,
      snapshotsAddRuntimeDependency := true,
      snapshotsIntegrations         := Seq.empty,
      snapshotsForceOverwrite       := false,
      snapshotsLocation             := sourceDirectory.value / "snapshots",
      snapshotsTemporaryDirectory := (
        Test / managedResourceDirectories
      ).value.head / "snapshots-tmp",
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
        val dest = (Test / managedSourceDirectories).value.head

        val sources = SnapshotsBuild.generateSources(
          projectId = snapshotsProjectIdentifier.value,
          packageName = snapshotsPackageName.value,
          snapshotsDestination =
            snapshotsLocation.value / snapshotsProjectIdentifier.value,
          sourceDestination = dest / "Snapshots.scala",
          tmpLocation = snapshotsTemporaryDirectory.value,
          forceOverwrite = snapshotsForceOverwrite.value
        )

        val integrations = snapshotsIntegrations.value.flatMap { integ =>
          SnapshotsBuild.generateIntegrationSources(
            dest / s"${integ}Integration.scala",
            integ,
            snapshotsPackageName.value
          )
        }

        sources ++ integrations
      },
      snapshotsMigrate := Def
        .inputTask {
          import complete.DefaultParsers._

          val migrations        = Seq("0.0.6").map(_.trim.toLowerCase())
          val args: Seq[String] = spaceDelimited("<arg>").parsed
          val migration = args.headOption
            .map(_.trim.toLowerCase())
            .filter(migrations.contains(_))
            .getOrElse(
              sys.error(
                s"Expected 1 argument to the task, which is migration name, one of: [${migrations.mkString(", ")}]"
              )
            )

          val log = sLog.value

          migration match {
            case "0.0.6" =>
              val oldLocation =
                (Test / resourceDirectory).value / "snapshots" / snapshotsProjectIdentifier.value
              val newLocation =
                snapshotsLocation.value / snapshotsProjectIdentifier.value

              if (oldLocation.exists() && oldLocation.isDirectory()) {
                val snapshots = IO.listFiles(oldLocation)
                log.info(
                  s"Found ${snapshots.size} snapshots in old location [$oldLocation]. Will migrate them to new location - [$newLocation]"
                )

                IO.createDirectory(newLocation)

                snapshots.foreach { file =>
                  IO.move(file, newLocation / file.name)
                }

                log.info(s"Migrated ${snapshots.size} files")

                val remaining = IO.listFiles(oldLocation).size

                if (remaining == 0) {
                  log.info(s"[$oldLocation] is now empty and can be removed")
                }

              } else {
                sLog.value.warn(
                  s"Tried to find old snapshots in [$oldLocation] but it doesn't seem to exist or is not a directory. Doing nothing"
                )
              }

          }

        }
        .tag(snapshotsTag)
        .evaluated
    )
}

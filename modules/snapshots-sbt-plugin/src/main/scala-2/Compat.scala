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

import sbt._

import Keys._

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

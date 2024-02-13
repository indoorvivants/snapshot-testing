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

package com.indoorvivants.snapshots

case class Snapshots(
    location: String,
    tmpLocation: String,
    forceOverwrite: Boolean
) extends Platform {

  private def getTmpFile(name: String) =
    tmpLocation.resolve(name)

  def sanitiseSnapshotName(name: String): String = {
    name.replaceAll("[^a-zA-Z0-9_\\-]", "_")
  }

  def recordChanges(name: String, contents: String, diff: String): Unit = {
    val saneName    = sanitiseSnapshotName(name)
    val tmpName     = saneName + "__snap.new"
    val tmpDiff     = saneName + "__snap.new.diff"
    val file        = location.resolve(saneName)
    val tmpFile     = tmpLocation.resolve(tmpName)
    val tmpFileDiff = tmpLocation.resolve(tmpDiff)

    val snapContents =
      saneName + "\n" + file + "\n" + contents

    tmpLocation.createDirectories()
    tmpFile.fileWriteContents(snapContents)
    tmpFileDiff.fileWriteContents(diff)
  }

  def write(name: String, contents: String): Unit = {
    val saneName = sanitiseSnapshotName(name)
    val file     = location.resolve(saneName)
    location.createDirectories()
    file.fileWriteContents(contents)
  }

  def clearChanges(name: String): Unit = {
    val saneName    = sanitiseSnapshotName(name)
    val tmpName     = saneName + "__snap.new"
    val tmpDiff     = saneName + "__snap.new.diff"
    val tmpFile     = getTmpFile(tmpName)
    val tmpFileDiff = getTmpFile(tmpDiff)

    tmpFileDiff.delete()
    tmpFile.delete()
  }

  def read(name: String): Option[String] = {
    val saneName = sanitiseSnapshotName(name)
    location.resolve(saneName).readFileContents()
  }

}

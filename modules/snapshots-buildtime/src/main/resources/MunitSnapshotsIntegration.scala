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

package com.indoorvivants.snapshots.munit_integration

import munit._
import com.indoorvivants.snapshots._
import munit.internal.difflib.Diffs
import example.Snapshots

// This is a sample integration for Munit
trait MunitSnapshotsIntegration {
  self: FunSuite =>

  def assertSnapshot(name: String, contents: String) = {
    Snapshots.read(name) match {
      case None =>
        Snapshots.recordChanges(
          name,
          contents,
          Diffs.create(contents, "").createDiffOnlyReport()
        )

      case Some(value) =>
        val diff = Diffs.create(contents, value)
        if (!diff.isEmpty) {
          val diffReport = diff.createDiffOnlyReport()
          Snapshots.recordChanges(name, contents, diffReport)
          Assertions.assertNoDiff(contents, value)
        } else
          Snapshots.clearChanges(name)
    }
  }
}

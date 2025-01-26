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

import _root_.$$PACKAGE$$.Snapshots

// This is a sample integration for Munit
trait MunitSnapshotsIntegration {
  self: munit.FunSuite =>

  /** Assert that the snapshot by the given name has given contents.
    *
    * If the snapshot file is missing, it will be eagerly written to the correct
    * location, the assertion will succeed.
    *
    * If the snapshot file exists, and its contents differ from the ones passed
    * into this function, then what happens next depends on the
    * `snapshotsForceOverwrite` setting in the SBT plugin:
    *
    *   - if `snapshotsForceOverwrite := true`, then the new contents will be
    *     directly written to the snapshot file. The assertion will succeed.
    *
    *   - if `snapshotsForceOverwrite := false`, then the diff will be recorded,
    *     and the snapshot will be marked as dirty, and you will need to resolve
    *     this change using one of the
    *     `snapshotsCheck/snapshotsAcceptAll/snapshotsDiscardAll` commands in
    *     the build tool. The assertion will fail.
    *
    * If the snapshot file exists, and its contents are the same as the ones
    * passed into this function, then assertion will succeed and the snapshot
    * will be marked as "clean" (if it was previously marked as dirty).
    *
    * @param name
    *   name of the snapshot, from which the snapshot filename will be derived
    *   by removing any characters other than English letters, numbers,
    *   underscore `_`, or hyphen `-`
    * @param contents
    */
  def assertSnapshot(name: String, contents: String) = {
    Snapshots.read(name) match {
      case None =>
        // If snapshot is not found, we directly write its contents
        Snapshots.write(name, contents)

      case Some(value) =>
        val diff = new munit.diff.Diff(contents, value)
        if (!diff.isEmpty) {
          if (!Snapshots.forceOverwrite) {
            val diffReport = diff.createDiffOnlyReport()
            Snapshots.recordChanges(name, contents, diffReport)
            munit.Assertions.assertNoDiff(contents, value)
          } else Snapshots.write(name, contents)
        } else
          Snapshots.clearChanges(name)
    }
  }
}

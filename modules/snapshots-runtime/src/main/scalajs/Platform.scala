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

import scala.scalajs.js.annotation.JSImport

import scalajs.js

private[snapshots] trait Platform {
  implicit class ToFileOps(s: String) {
    def resolve(segment: String): String =
      s + "/" + segment

    def fileWriteContents(contents: String): Unit =
      FS.writeFileSync(s, contents)

    def delete(): Unit =
      FS.rmSync(s, js.Dynamic.literal(force = true))

    def readFileContents(): Option[String] = {
      val exists =
        FS.statSync(
          s,
          js.Dynamic.literal(throwIfNoEntry = false)
        ) != js.undefined
      if (!exists) None
      else
        Some {
          FS.readFileSync(s, js.Dynamic.literal(encoding = "utf8"))
        }
    }
  }

}

@js.native
private[snapshots] trait FS extends js.Object {
  def readFileSync(path: String, options: js.Object = js.Object()): String =
    js.native
  def rmSync(path: String, options: js.Object = js.Object()): String =
    js.native
  def writeFileSync(
      path: String,
      contents: String,
      options: String = ""
  ): Unit = js.native
  def statSync(path: String, options: js.Any): js.Any = js.native
}

@js.native
@JSImport("node:fs", JSImport.Namespace)
private[snapshots] object FS extends FS

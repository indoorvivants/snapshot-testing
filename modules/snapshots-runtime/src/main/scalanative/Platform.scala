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

import java.io.File
import java.io.FileWriter
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

private[snapshots] trait Platform {
  implicit class ToFileOps(s: String) {
    def resolve(segment: String): String =
      Paths.get(s).resolve(segment).toString()

    def fileWriteContents(contents: String, encoding: String): Unit = {
      val f = Files.newBufferedWriter(Paths.get(s), Charset.forName(encoding))
      try {
        f.write(contents)
      } finally { f.close() }
    }

    def delete(): Unit =
      new File(s).delete()

    def createDirectories(): Unit =
      Files.createDirectories(Paths.get(s))

    def readFileContents(encoding: String): Option[String] = {
      val file = new File(s)
      if (!file.exists()) None
      else
        Some {
          scala.io.Source
            .fromFile(file, encoding)
            .getLines()
            .mkString("\n")
        }
    }
  }
}

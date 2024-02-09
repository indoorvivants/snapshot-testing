package com.indoorvivants.snapshots

import java.nio.file.Paths
import java.io.FileWriter
import java.io.File

private[snapshots] trait Platform {
  // extension (s: String)
  implicit class ToFileOps(s: String) {
    def resolve(segment: String): String =
      Paths.get(s).resolve(segment).toString()

    def fileWriteContents(contents: String): Unit = {
      val f = new FileWriter(new File(s))
      try {
        f.write(contents)
      } finally { f.close() }
    }

    def delete(): Unit =
      new File(s).delete()

    def readFileContents(): Option[String] = {
      val file = new File(s)
      if (!file.exists()) None
      else
        Some {
          scala.io.Source
            .fromFile(file, "utf-8")
            .getLines()
            .mkString("\n")
        }
    }
  }
}

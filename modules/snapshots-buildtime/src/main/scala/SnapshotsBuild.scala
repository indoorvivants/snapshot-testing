package com.indoorvivants.snapshots.build

import java.io.File
import java.nio.file.Files
import scala.io.StdIn
import java.io.FileWriter

object SnapshotsBuild {

  private object IO {
    def listFiles(loc: File) = {
      val files = List.newBuilder[File]

      Files
        .walk(loc.toPath())
        .forEach(path => {
          files += path.toFile()
        })

      files.result()
    }

    def writeLines(loc: File, lines: Seq[String]) = {
      val f = new FileWriter(loc)
      try
        lines.foreach(l => f.write(l + "\n"))
      finally
        f.close()
    }
  }
  def checkSnapshots(tmpLocation: File, projectId: String) = {
    val bold  = scala.Console.BOLD
    val reset = scala.Console.RESET
    val legend =
      s"${bold}a${reset} - accept, ${bold}s${reset} - skip\nYour choice: "
    val modified = IO
      .listFiles(
        tmpLocation
      )

    if (modified.isEmpty) {
      System.err.println(
        s"No snapshots to check in [${projectId}]"
      )
    } else {

      modified
        .filter(_.getName.endsWith("__snap.new"))
        .foreach { f =>
          val diffFile = new File(f.toString() + ".diff")
          assert(diffFile.exists(), s"Diff file $diffFile not found")

          val diffContents = scala.io.Source
            .fromFile(diffFile)
            .getLines()
            .mkString(System.lineSeparator())

          val snapshotName :: destination :: newContentsLines =
            scala.io.Source.fromFile(f).getLines().toList

          println(
            s"Project ID: ${bold}${projectId}${reset}"
          )
          println(
            s"Name: ${scala.Console.BOLD}$snapshotName${scala.Console.RESET}"
          )
          println(
            s"Path: ${scala.Console.BOLD}$destination${scala.Console.RESET}"
          )
          println(diffContents)

          println("\n\n")
          print(legend)

          val choice = StdIn.readLine().trim

          if (choice == "a") {
            IO.writeLines(new File(destination), newContentsLines)
            f.delete()
            diffFile.delete()
          }

        }
    }

  }

  def generateSources(
      projectId: String,
      packageName: String,
      snapshotsDestination: File,
      sourceDestination: File,
      tmpLocation: File
  ) = {

    IO.writeLines(
      sourceDestination,
      SnapshotsGenerate(
        snapshotsDestination,
        tmpLocation,
        packageName
      ).linesIterator.toList
    )

    Files.createDirectory(snapshotsDestination.toPath)
    Files.createDirectory(tmpLocation.toPath)

    Seq(sourceDestination)
  }

  private def SnapshotsGenerate(
      path: File,
      tempPath: File,
      packageName: String
  ) =
    s"""
     |package $packageName
     |object Snapshots extends proompts.snapshots.Snapshots(location = "$path", tmpLocation = "$tempPath")
      """.trim.stripMargin

}

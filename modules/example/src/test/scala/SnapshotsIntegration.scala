import munit._
import com.indoorvivants.snapshots._
import munit.internal.difflib.Diffs
import munit.Assertions
import munit.FunSuite
import example.Snapshots

// This is a sample integration for Munit
trait SnapshotsIntegration {
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

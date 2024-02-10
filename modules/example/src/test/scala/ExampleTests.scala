import munit._

import com.indoorvivants.snapshots._

import munit.internal.difflib.Diffs
import munit.Assertions
import munit.FunSuite
import example.Snapshots

class ExampleTests extends FunSuite with SnapshotsIntegration {
  test("hello") {
    assertSnapshot("my.snapshot", "hello?")
  }
}

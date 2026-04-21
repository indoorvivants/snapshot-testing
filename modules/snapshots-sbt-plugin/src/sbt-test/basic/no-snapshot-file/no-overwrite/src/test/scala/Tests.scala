package example.bla

import com.indoorvivants.snapshots.munit_integration._
import munit._

class MunitExampleTests extends FunSuite with MunitSnapshotsIntegration {
  test("hello") {
    assertSnapshot("my.snapshot", "hello - more stuff")
  }
}

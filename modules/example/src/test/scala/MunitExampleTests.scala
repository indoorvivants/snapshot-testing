import com.indoorvivants.snapshots.munit_integration._
import munit._

class MunitExampleTests extends FunSuite with MunitSnapshotsIntegration {
  test("hello") {
    assertSnapshot("my.snapshot", "hello - more stuff")
  }
  test("hello unicode 👌") {
    assertSnapshot("my.snapshot.utf8", "hello - 👌")
  }
}

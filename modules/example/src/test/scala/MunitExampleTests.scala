import com.indoorvivants.snapshots._
import munit._

class MunitExampleTests extends FunSuite with MunitSnapshotsIntegration {
  test("hello") {
    assertSnapshot("my.snapshot", "hello - more stuff")
  }
}

import com.indoorvivants.snapshots.munit_integration.*
import munit.*

import upickle.default.*

case class Test(s: String) derives ReadWriter

enum MyStuff derives ReadWriter:
  case Hello(a: Int, b: Test)
  case Bla(x: Boolean)

class JsonGoldenTests extends FunSuite with MunitSnapshotsIntegration:
  test("json tests") {
    val obj = MyStuff.Hello(25, Test("yo"))
    assertSnapshot("json.codec", write(obj, indent = 4))
  }

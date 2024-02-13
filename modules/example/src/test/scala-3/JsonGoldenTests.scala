import com.indoorvivants.snapshots.munit_integration.*
import munit.*

import io.circe.*, io.circe.syntax.*

case class Test(s: String)
enum MyStuff derives Codec.AsObject:
  case Hello(a: Int, b: Test)
  case Bla(x: Boolean)

class JsonGoldenTests extends FunSuite with MunitSnapshotsIntegration:
  test("json tests") {
    val obj = MyStuff.Hello(25, Test("yo"))
    assertSnapshot("json.codec", obj.asJson.spaces4)
  }

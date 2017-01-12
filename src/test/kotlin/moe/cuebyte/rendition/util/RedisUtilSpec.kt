package moe.cuebyte.rendition.util

import moe.cuebyte.rendition.Model
import moe.cuebyte.rendition.mock.BookStr
import moe.cuebyte.rendition.type.int
import moe.cuebyte.rendition.type.long
import moe.cuebyte.rendition.type.string
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import kotlin.test.assertEquals

object RedisUtilSpec : Spek({
  describe("RedisUtilSpec") {
    on("gen key or id") {
      it("gen id") {
        assertEquals(genId(BookStr, "abc"), "${BookStr.name}:abc")
      }
      it("gen hash key") {
        assertEquals(genKey(BookStr, BookStr.stringIndices[0], "Shakes"),
            "${BookStr.name}:${BookStr.stringIndices[0].name}:Shakes")
      }
      it("gen sorted set key") {
        assertEquals(genKey(BookStr, BookStr.numberIndices[0]),
            "${BookStr.name}:${BookStr.numberIndices[0].name}")
      }
    }
  }
})
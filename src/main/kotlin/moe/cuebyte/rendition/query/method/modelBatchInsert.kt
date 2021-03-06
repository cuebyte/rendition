@file:JvmName("ModelMethod")
@file:JvmMultifileClass
package moe.cuebyte.rendition.query.method

import moe.cuebyte.rendition.Model
import moe.cuebyte.rendition.query.data.BatchInsertData
import moe.cuebyte.rendition.util.Connection
import moe.cuebyte.rendition.util.genId
import moe.cuebyte.rendition.util.genKey

fun Model.batchInsert(batch: List<Map<String, Any>>): Boolean {
  val bInsert = BatchInsertData(this, batch)
  val t = Connection.get().multi()
  
  for (body in bInsert.bodies) {
    t.hmset(genId(this, body[this.pk.name]!!), body)
  }
  for ((col, idsMap) in bInsert.strMultiIndex) {
    for ((idxName, ids) in idsMap) {
      t.sadd(genKey(this, col, idxName), *ids.toTypedArray())
    }
  }
  for ((col, idsMap) in bInsert.numMultiIndex) {
    for ((idxName, ids) in idsMap) {
      for (id in ids) {
        t.zadd(genKey(this, col), idxName, id)
      }
    }
  }
  return !t.exec().isEmpty()
}
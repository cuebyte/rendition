package moe.cuebyte.rendition

import java.util.ArrayList
import java.util.HashMap

internal data class FoolFourReturn(
    val pk: Column,
    val sIndex: List<Column>,
    val dIndex: List<Column>,
    val columns: List<Column>
)

abstract class Model {
  val name: String
  val pk: Column
  val stringIndices: List<Column>
  val doubleIndices: List<Column>
  val columns: List<Column>

  constructor(name: String, schema: MutableMap<String, IncompleteColumn>) {
    this.name = name
    val (a, b, c, d) = initIndex(schema)
    pk = a; stringIndices = b; doubleIndices = c; columns = d
  }

  constructor(name: String, body: (MutableMap<String, IncompleteColumn>)->Unit) {
    this.name = name
    val schema: MutableMap<String, IncompleteColumn> = HashMap()
    println(schema.keys.isEmpty())
    val (a, b, c, d) = initIndex(schema)
    pk = a; stringIndices = b; doubleIndices = c; columns = d
  }


  private fun initIndex(schema: Map<String, IncompleteColumn>): FoolFourReturn {
    var tPk: Column? = null
    val tStringIndices: MutableList<Column> = ArrayList()
    val tDoubleIndices: MutableList<Column> = ArrayList()
    val tColumns: MutableList<Column> = ArrayList()

    schema.forEach {
      val col: Column = it.value.complete(it.key)
      tColumns.add(col)
      when (col.info) {
        IncompleteColumn.Info.NONE -> {
        }
        IncompleteColumn.Info.STRING_PK -> {
          tPk = col; tPk!!.automated = false
        }
        IncompleteColumn.Info.DOUBLE_PK -> {
          tPk = col; tDoubleIndices.add(col)
        }
        IncompleteColumn.Info.STRING_INDEX -> tStringIndices.add(col)
        IncompleteColumn.Info.DOUBLE_INDEX -> tDoubleIndices.add(col)
      }
    }
    tPk ?: throw Exception("No primary key in schema.")
    return FoolFourReturn(tPk!!, tStringIndices, tDoubleIndices, tColumns)
  }
}
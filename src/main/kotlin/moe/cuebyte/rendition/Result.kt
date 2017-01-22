@file:JvmName("Result")
@file:JvmMultifileClass

package moe.cuebyte.rendition

import redis.clients.jedis.Response
import java.util.TreeSet

class Result(val model: Model, private val resp: Response<Map<String, String>>)
  : Map<String, Any> {

  override val entries get() = lazyMap.entries
  override val values get() = lazyMap.values
  override val keys get() = lazyMap.keys
  override val size get() = lazyMap.size

  override fun containsValue(value: Any) = lazyMap.containsValue(value)
  override fun containsKey(key: String) = lazyMap.containsKey(key)
  override fun get(key: String) = lazyMap[key]
  override fun isEmpty() = lazyMap.isEmpty()

  override fun toString() = lazyMap.toString()

  internal val id: String get() = lazyMap[pkName].toString()

  private val pkName: String = model.pk.name
  private val lazyMap: Map<String, Any> by lazy { init() }

  private fun init(): Map<String, Any> {
    val data = resp.get()
    return model.columns.map { (name, col) ->
      val datum = data[name]!!
      when (col.type) {
        String::class.java -> name to datum
        Int::class.java -> name to datum.toInt()
        Double::class.java -> name to datum.toDouble()
        Boolean::class.java -> name to datum.toBoolean()
        Float::class.java -> name to datum.toFloat()
        Long::class.java -> name to datum.toLong()
        else -> throw Exception("Internal error")
      }
    }.toMap()
  }
}

class ResultSet : HashSet<Result> {
  val model: Model

  constructor(model: Model) : super() {
    this.model = model
  }

  private constructor(model: Model, results: List<Result>) : super(results) {
    this.model = model
  }

  override fun toString(): String {
    var str = "[\n"
    for (res in this) {
      str += "${res.toString()},\n"
    }
    str += "]\n"
    return str
  }

  infix fun AND(resultSet: ResultSet): Calculator
      = Calculator(this).addState(Calculator.Op.AND, resultSet)

  infix fun OR(resultSet: ResultSet): Calculator
      = Calculator(this).addState(Calculator.Op.OR, resultSet)

  infix fun AND(calc: Calculator): Calculator
      = Calculator(this).cat(moe.cuebyte.rendition.Calculator.Op.AND, calc)

  infix fun OR(calc: Calculator): Calculator
      = Calculator(this).cat(moe.cuebyte.rendition.Calculator.Op.OR, calc)

  internal fun intersect(resultSet: ResultSet): ResultSet {
    val (bigger, smaller) = getPair(this, resultSet)
    val set = TreeSet(bigger.map(Result::id))
    set.retainAll(smaller.map(Result::id))
    return ResultSet(model, this.filter { it.id in set })
  }

  internal fun union(resultSet: ResultSet): ResultSet {
    val (bigger, smaller) = getPair(this, resultSet)
    val baseIdTree = bigger.map(Result::id).toSortedSet()
    for (result in smaller) { // For the performance,
      if (result.id !in baseIdTree) {
        bigger.add(result)
      }
    }
    return bigger
  }

  /**
   * @return Pair(Bigger, Smaller)
   */
  private fun getPair(a: ResultSet, b: ResultSet): Pair<ResultSet, ResultSet> {
    return if (a.size > b.size) {
      Pair<ResultSet, ResultSet>(a, b)
    } else {
      Pair<ResultSet, ResultSet>(b, a)
    }
  }
}
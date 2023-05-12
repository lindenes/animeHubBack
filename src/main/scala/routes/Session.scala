package routes

import cats.effect.*
import data.sqlquery.PersonQuery.UserInfo

import scala.collection.mutable
import scala.util.Try
object Session {

  private val cache: ThreadLocal[mutable.Map[String, Int]] = ThreadLocal.withInitial(() => mutable.Map.empty)

  def get(key: String): Option[Int] = Try(cache.get().get(key)).getOrElse(None)

  def put(key: String, value: Int): Unit = cache.get().put(key, value)

  def remove(key: String): Unit = cache.get().remove(key)

  def clear(): Unit = cache.get().clear()

}

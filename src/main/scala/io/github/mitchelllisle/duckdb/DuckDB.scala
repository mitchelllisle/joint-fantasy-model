package io.github.mitchelllisle.duckdb

import org.duckdb.{DuckDBAppender, DuckDBConnection}

import scala.reflect.runtime.universe._
import scala.reflect.runtime.currentMirror
import scala.reflect.ClassTag
import java.sql.DriverManager

object ScalaToDuckDBTypeMapper {
  private def mapType(tpe: Type): String = tpe match {
    case t if t =:= typeOf[Int] => "INTEGER"
    case t if t =:= typeOf[Long] => "BIGINT"
    case t if t =:= typeOf[Double] => "DOUBLE"
    case t if t =:= typeOf[Float] => "FLOAT"
    case t if t =:= typeOf[String] => "VARCHAR"
    case t if t =:= typeOf[Boolean] => "BOOLEAN"
    case t if t <:< typeOf[Option[_]] => s"${mapType(t.typeArgs.head)} NULL"
    case t if t <:< typeOf[Product] => mapCaseClass(t)
    case _ => throw new IllegalArgumentException(s"Unsupported type: $tpe")
  }

  def mapCaseClass(tpe: Type): String = {
    tpe.decls.collect {
      case m: MethodSymbol if m.isCaseAccessor =>
        s"${m.name.toString.trim} ${mapType(m.returnType)}"
    }.mkString(", ")
  }
}

class DuckDB(path: String) {
  private val connection: DuckDBConnection = DriverManager.getConnection(s"jdbc:duckdb:$path").asInstanceOf[DuckDBConnection]

  private def tableName[T: TypeTag]: String = typeOf[T].typeSymbol.name.toString

  def createTable[T: TypeTag](): Unit = {
    val tpe = typeOf[T]
    val fields = ScalaToDuckDBTypeMapper.mapCaseClass(tpe)
    val createStmt = s"CREATE TABLE IF NOT EXISTS ${tableName[T]} ($fields)"
    val stmt = connection.createStatement()
    stmt.execute(createStmt)
    stmt.close()
  }

  def append[T <: Product : TypeTag : ClassTag](instances: List[T]): Unit = {
    val appender = new DuckDBAppender(connection, "main", tableName[T])

    instances.foreach { instance =>
      appender.beginRow()
      instance.productIterator.foreach {
        case Some(value: Int) => appender.append(value)
        case Some(value: Long) => appender.append(value)
        case Some(value: Double) => appender.append(value)
        case Some(value: Float) => appender.append(value)
        case Some(value: String) => appender.append(value)
        case Some(value: Boolean) => appender.append(value)
        case value: Int => appender.append(value)
        case value: Long => appender.append(value)
        case value: Double => appender.append(value)
        case value: Float => appender.append(value)
        case value: String => appender.append(value)
        case value: Boolean => appender.append(value)
        case _ => throw new IllegalArgumentException("Unsupported type")
      }
      appender.endRow()
    }
    appender.close()
  }

  def read[T: TypeTag: ClassTag](): List[T] = {
    val tpe = typeOf[T]
    val constructor = tpe.decl(termNames.CONSTRUCTOR).asMethod
    val classMirror = currentMirror.reflectClass(tpe.typeSymbol.asClass)
    val stmt = connection.createStatement()
    val rs = stmt.executeQuery(s"SELECT * FROM ${tableName[T]}")
    val result = Iterator.continually((rs.next(), rs)).takeWhile(_._1).map { case (_, rs) =>
      val args = constructor.paramLists.flatten.map { param =>
        val name = param.name.toString
        val value = rs.getObject(name)
        param.typeSignature match {
          case t if t =:= typeOf[Int] => value.asInstanceOf[Int]
          case t if t =:= typeOf[Long] => value.asInstanceOf[Long]
          case t if t =:= typeOf[Double] => value.asInstanceOf[Double]
          case t if t =:= typeOf[Float] => value.asInstanceOf[Float]
          case t if t =:= typeOf[String] => value.asInstanceOf[String]
          case t if t =:= typeOf[Boolean] => value.asInstanceOf[Boolean]
          case t if t <:< typeOf[Option[_]] => Option(value)
          case _ => value
        }
      }
      classMirror.reflectConstructor(constructor)(args: _*).asInstanceOf[T]
    }.toList
    stmt.close()
    result
  }

  def exportDatabase(path: String): Unit = {
    val stmt = connection.createStatement()
    stmt.execute(s"EXPORT DATABASE '$path'")
    stmt.close()
  }
}
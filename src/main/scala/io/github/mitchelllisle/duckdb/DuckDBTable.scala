package io.github.mitchelllisle.duckdb

import org.duckdb.DuckDBConnection
import scala.reflect.runtime.universe._

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

trait DuckDBTable {
  private var connection: Option[DuckDBConnection] = None

  def withConnection(conn: DuckDBConnection): this.type = {
    connection = Some(conn)
    this
  }

  private def tableName: String = this.getClass.getSimpleName

  def createTable()(implicit tag: TypeTag[this.type]): DuckDBTable = {
    val conn = getConnection
    val tpe = typeOf[this.type]
    val fields = ScalaToDuckDBTypeMapper.mapCaseClass(tpe)
    val createStmt = s"CREATE TABLE $tableName ($fields)"
    val stmt = conn.createStatement()
    stmt.execute(createStmt)
    stmt.close()
    this
  }

  private def getConnection: DuckDBConnection = connection.getOrElse(throw new IllegalStateException("Connection not set"))

  def exportTable(filePath: String): Unit = {
    val conn = getConnection
    val exportStmt = s"COPY $tableName TO '$filePath' (FORMAT CSV, HEADER)"
    val stmt = conn.createStatement()
    stmt.execute(exportStmt)
    stmt.close()
  }
}

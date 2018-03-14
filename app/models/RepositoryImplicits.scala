package models

import java.sql
import java.time.{LocalDate, LocalDateTime}

import models.User.{Female, Gender, Male}
import slick.basic.DatabaseConfig
import slick.jdbc.{JdbcProfile, PostgresProfile}

trait RepositoryImplicits {
  type Profile <: JdbcProfile
  val dbConfig: DatabaseConfig[PostgresProfile]

  import dbConfig._
  import profile.api._

  // 所有映射函数需要延迟

  /**
    * 自动转换Gender对象
    */
  implicit lazy val genderColumnType: BaseColumnType[Gender] =
    MappedColumnType.base[Gender, String](
      gender => if (gender == Male) "M" else "F",
      s => if (s == "M") Male else Female)

  implicit lazy val localDateColumnType: BaseColumnType[LocalDate] =
    MappedColumnType.base[LocalDate, sql.Date](
      ld => if (ld != null) sql.Date.valueOf(ld) else null,
      date => if (date != null) date.toLocalDate else null)

  implicit lazy val localDateTimeColumnType: BaseColumnType[LocalDateTime] =
    MappedColumnType.base[LocalDateTime, sql.Timestamp](
      ldt => if (ldt != null) sql.Timestamp.valueOf(ldt) else null,
      timestamp => if (timestamp != null) timestamp.toLocalDateTime else null)
}
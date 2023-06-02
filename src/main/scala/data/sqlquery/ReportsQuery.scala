package data.sqlquery

import _root_.data.PersonRole
import cats.effect.IO
import doobie.*
import doobie.implicits.*
import io.circe.literal.json
import io.circe.Json

object ReportsQuery:

  private case class Reports(
      reportId: Int,
      createdAt: String,
      commentId: String,
      reportsCount: Int,
  )

  private val xa = Transactor.fromDriverManager[IO](
    "com.mysql.cj.jdbc.Driver",
    "jdbc:mysql://127.0.0.1/animeHub",
    "root",
    ",tkstudjplbrb",
  )

  def getReportList(personId: Int): IO[List[Json]] =
    for
      person <- PersonQuery.getCurrentUser(personId)
      reports <-
        if person.get.role == PersonRole.Admin.ordinal ||
          person.get.role == PersonRole.Moderator.ordinal
        then
          sql"SELECT * FROM `report`"
            .query[Reports]
            .to[List]
            .transact(xa)
            .map { reports =>
              reports.map(report =>
                json"""{"reportId": ${report.reportId}, "createdAt": ${report
                    .createdAt}, "commentId":  ${report
                    .commentId}, "reportsCount": ${report.reportsCount}}"""
              )
            }
        else IO.pure(List(json"""{"success": "false"}"""))
    yield reports

  def addReport(commentId: Int): IO[Json] =
    for
      isExist <-
        sql"SELECT reports_count FROM `report` WHERE comment_id = $commentId"
          .query[Int]
          .option
          .transact(xa)
      _ <-
        if isExist.nonEmpty then
          sql"INSERT INTO `report` (comment_id, reports_count) VALUES ($commentId, 0)"
            .update
            .run
            .transact(xa)
            .attemptSql
        else
          sql"UPDATE `report` SET reports_count = reports_count + 1 WHERE comment_id = $commentId"
            .update
            .run
            .transact(xa)
            .attemptSql
    yield json"""{"success": "true"}"""

  def delReport(reportId: Int): IO[Boolean] =
    sql"DELETE FROM `report` WHERE id=$reportId"
      .update
      .run
      .transact(xa)
      .attemptSql
      .map {
        case Right(_) => true
        case Left(e) => false
      }

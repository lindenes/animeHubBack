package data.sqlquery

import io.circe.Json
import io.circe.literal.json
import cats.effect.{IO, IOApp}
import data.sqlquery.CommentQuery.xa
import doobie.*
import doobie.implicits.*
import doobie.util.ExecutionContexts
import data.{PersonRole, sqlquery}
object ReportsQuery {

  case class Reports(reportId:Int, createdAt:String, commentId:String, postId:Int, reportsCount:Int, commentText:String, postTitle:String, commentAuthor:String)

  val xa = Transactor.fromDriverManager[IO](
    "com.mysql.cj.jdbc.Driver",
    "jdbc:mysql://127.0.0.1/animeHub",
    "root",
    ",tkstudjplbrb",
  )

  def getReportList(personId:Int):IO[List[Json]] =
    for{
      person <- PersonQuery.getCurrentUser(personId)
      reports <- if(person.get.role == PersonRole.Admin.ordinal || person.get.role == PersonRole.Moderator.ordinal)
        sql"SELECT report.*, comment.text, post.title, user.login FROM report INNER JOIN comment ON report.comment_id = comment.id INNER JOIN post ON report.post_id = post.id INNER JOIN `user` ON comment.user_id = user.id"
          .query[Reports]
          .to[List]
          .transact(xa)
          .map{ reports =>{
              reports.map( report =>
                json"""{"reportId": ${report.reportId}, "createdAt": ${report.createdAt}, "commentId":  ${report.commentId}, "reportsCount": ${report.reportsCount}, "postId": ${report.postId}, "postTitle":  ${report.postTitle}, "commentText":  ${report.commentText}, "commentAuthor": ${report.commentAuthor}}"""
              )
            }
          }
        else
          IO.pure(List(json"""{"success": "false"}"""))
    }yield reports
  def addReport(commentId:Int, postId:Int):IO[Json] =
    for{
      isExist <- sql"SELECT reports_count FROM `report` WHERE comment_id = $commentId"
        .query[Int]
        .option
        .transact(xa)
      _ <- if(isExist.nonEmpty)
        sql"UPDATE `report` SET reports_count = reports_count + 1 WHERE comment_id = $commentId"
          .update
          .run
          .transact(xa)
          .attemptSql
      else
        sql"INSERT INTO `report` (comment_id, post_id, reports_count) VALUES ($commentId, $postId, 1)"
          .update
          .run
          .transact(xa)
          .attemptSql
    }yield json"""{"success": "true"}"""

  def delReport(reportId:Int):IO[Boolean]=

    sql"DELETE FROM `report` WHERE id=$reportId"
      .update
      .run
      .transact(xa)
      .attemptSql
      .map{
        case Right(_) => true
        case Left(e) => false
      }

  def delReports(postId:Int):IO[Boolean]=
    sql"DELETE FROM `report` WHERE post_id=$postId"
      .update
      .run
      .transact(xa)
      .attemptSql
      .map {
        case Right(_) => true
        case Left(e) => false
      }

}

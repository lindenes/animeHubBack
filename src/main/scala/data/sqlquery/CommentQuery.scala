package data.sqlquery

import cats.effect.{IO, IOApp}
import doobie.*
import doobie.implicits.*
import doobie.util.ExecutionContexts
import io.circe.Json
import io.circe.literal.json

object CommentQuery {
  case class Comment(id:Int, createdAt:String, text:String, userId:String, postId:String, login:String)

  def getCommentList(postId:Int):IO[Json]=

    val xa = Transactor.fromDriverManager[IO](
      "com.mysql.cj.jdbc.Driver",
      "jdbc:mysql://127.0.0.1/animeHub",
      "root",
      "",
    )
    sql"SELECT comment.*, user.login FROM comment INNER JOIN user ON comment.user_id = user.id WHERE comment.post_id = $postId;"
      .query[Comment]
      .to[List]
      .transact(xa)
      .map( posts => json"""{"Comments": ${
        posts.map{
          elem=>
            json"""{"commentId": ${elem.id}, "createdAt":  ${elem.createdAt}, "text":  ${elem.text}, "userId": ${elem.userId}, "postId":  ${elem.postId}, "userLogin":  ${elem.login}}"""
        }
      }}"""
      )
      .handleErrorWith(
        e => IO.pure(json"""{"getCommentError":"Ошибка вывода комментариев", "details": ${e.toString}}""")
      )

  def addComment(userId: Int, postId: Int, text:String):IO[Json] =

    val xa = Transactor.fromDriverManager[IO](
      "com.mysql.cj.jdbc.Driver",
      "jdbc:mysql://127.0.0.1/animeHub",
      "root",
      "",
    )

    sql"INSERT INTO `comment` (`text`, `user_id`, `post_id`) VALUES ($text, $userId, $postId)"
      .update
      .run
      .transact(xa)
      .attemptSql
      .map {
        case Right(_) => json"""{"success": "true"}"""
        case Left(e) => json"""{"success":  "false"}"""
      }
    
}

package data.sqlquery

import cats.effect.{IO, IOApp}
import doobie.*
import doobie.implicits.*
import doobie.util.ExecutionContexts
import io.circe.Json
import io.circe.literal.json

object CommentQuery {

  val xa = Transactor.fromDriverManager[IO](
    "com.mysql.cj.jdbc.Driver",
    "jdbc:mysql://127.0.0.1/animeHub",
    "root",
    ",tkstudjplbrb",
  )
  case class Comment(id:Int, createdAt:String, text:String, userId:String, postId:String, login:String, avatarPath:String, title:String)

  def getCommentList(postId:Int):IO[Json]=

    sql"SELECT comment.*, user.login, user.avatar_path FROM comment INNER JOIN user ON comment.user_id = user.id WHERE comment.post_id = $postId;"
      .query[Comment]
      .to[List]
      .transact(xa)
      .map( comments => json"""{"Comments": ${
        comments.map{
          elem=>
            json"""{"commentId": ${elem.id}, "createdAt":  ${elem.createdAt}, "text":  ${elem.text}, "userId": ${elem.userId}, "postId":  ${elem.postId}, "userLogin":  ${elem.login}, "imagePath":  ${elem.avatarPath}}"""
        }
      }}"""
      )
      .handleErrorWith(
        e => IO.pure(json"""{"getCommentError":"Ошибка вывода комментариев", "details": ${e.toString}}""")
      )

  def addComment(userId: Int, postId: Int, text:String):IO[Json] =

    sql"INSERT INTO `comment` (`text`, `user_id`, `post_id`) VALUES ($text, $userId, $postId)"
      .update
      .run
      .transact(xa)
      .attemptSql
      .map {
        case Right(_) => json"""{"success": "true"}"""
        case Left(e) => json"""{"success":  "false"}"""
      }

  def getPersonComments(personId:Int):IO[List[Json]]=

    sql"SELECT comment.*, user.login, user.avatar_path, post.title FROM comment INNER JOIN user ON comment.user_id = user.id INNER JOIN post ON post.id = comment.post_id WHERE comment.user_id = $personId"
      .query[Comment]
      .to[List]
      .transact(xa)
      .map{ comments =>
        comments.map( comment =>
          json"""{"commentId": ${comment.id}, "createdAt":  ${comment.createdAt}, "text":  ${comment.text}, "title":${comment.title}, "userId": ${comment.userId}, "postId":  ${comment.postId}, "userLogin":  ${comment.login}, "imagePath":  ${comment.avatarPath}}"""
        )
      }
      .handleErrorWith(e => IO.pure( List.empty[Json] ))

  def delComment(commentId: Int): IO[Boolean] =

    sql"DELETE FROM `comment` WHERE id=$commentId"
      .update
      .run
      .transact(xa)
      .attemptSql
      .map {
        case Right(_) => true
        case Left(e) => false
      }
    
}

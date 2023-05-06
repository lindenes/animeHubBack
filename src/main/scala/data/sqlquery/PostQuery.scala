package data.sqlquery

import cats.effect.{IO, IOApp}
import org.http4s.Request

import scala.concurrent.ExecutionContext.Implicits.global
import doobie.*
import doobie.implicits.*
import doobie.util.ExecutionContexts
import io.circe.Json
import io.circe.literal.json

import java.sql.{DriverManager, SQLException}
object PostQuery{
  case class Post(id:Int, createdAt:String, title:String, description:String, year:String, imagePath:String,
                  videoPath:String, episodeCount:Int, episodeDuration:Int, userId:Int, typeId:Int, rating:Double)

  def getPostList: IO[Json] =

    getPostListDB.flatMap{
      case Left(value) => IO.pure(
        json"""{"postInfo":  ${value.map(elem =>
          json"""{ "id": ${elem.id},"createdAt": ${elem.createdAt}, "title": ${elem.title},
                "description": ${elem.description},"year": ${elem.year}, "imagePath": ${elem.imagePath},
                  "videoPath": ${elem.videoPath},"episodeCount": ${elem.episodeCount}, "episodeDuration":${elem.episodeDuration},
                    "userId": ${elem.userId}, "typeId": ${elem.typeId}, "rating": ${elem.rating} }""")}}"""
      )
      case Right(value) => IO.pure(
        json"""{"postError": ${value}}"""
      )

    }
 private def getPostListDB: IO[Either[List[Post], String]] =

   val xa = Transactor.fromDriverManager[IO](
     "com.mysql.cj.jdbc.Driver",
     "jdbc:mysql://127.0.0.1/animeHub",
     "root",
     "",
   )

   sql"SELECT * FROM post LIMIT 10"
     .query[Post]
     .to[List]
     .transact(xa)
     .map(posts => Left(posts))
     .handleErrorWith(e => IO(Right("Ошибка вывода постов")))

 def getPostById(id:Int):IO[Json] =

   val xa = Transactor.fromDriverManager[IO](
     "com.mysql.cj.jdbc.Driver",
     "jdbc:mysql://127.0.0.1/animeHub",
     "root",
     "",
   )

   sql"SELECT * FROM post WHERE id = $id"
     .query[Post]
     .option
     .transact(xa)
     .map {
       case Some(post:Post) => json"""{ "id": ${post.id},"createdAt": ${post.createdAt}, "title": ${post.title},
               "description": ${post.description},"year": ${post.year}, "imagePath": ${post.imagePath},
                 "videoPath": ${post.videoPath},"episodeCount": ${post.episodeCount}, "episodeDuration":${post.episodeDuration},
                   "userId": ${post.userId}, "typeId": ${post.typeId}, "rating": ${post.rating} }"""

       case None => json"""{"postGetError": "Такого поста не существует"}"""

     }

//  def getPostListJDBCAsync(): IO[Either[Post, String]] =
//    val url = "jdbc:mysql://127.0.0.1/animeHub"
//    val username = "root"
//    val password = ""
//    for {
//      connection <- IO.delay(DriverManager.getConnection(url, username, password))
//      query = "SELECT * FROM post LIMIT 10"
//      postList <- IO.delay {
//        val preparedStatement = connection.prepareStatement(query)
//        val resultSet = preparedStatement.executeQuery()
//        if (resultSet.next()) {
//          Left(
//            Post(
//              id = resultSet.getInt("id"),
//              createdAt = resultSet.getString("created_at"),
//              title = resultSet.getString("title"),
//              description = resultSet.getString("description"),
//              year = resultSet.getString("year"),
//              imagePath = resultSet.getString("image_path"),
//              videoPath = resultSet.getString("video_path"),
//              episodeCount = resultSet.getInt("episode_count"),
//              episodeDuration = resultSet.getInt("episode_duration"),
//              userId = resultSet.getInt("user_id"),
//              typeId = resultSet.getInt("type_id"),
//              rating = resultSet.getDouble("rating")
//            )
//          )
//        } else {
//          Right(
//            "Ошибка вывода постов"
//          )
//        }
//      }
//    } yield postList

}

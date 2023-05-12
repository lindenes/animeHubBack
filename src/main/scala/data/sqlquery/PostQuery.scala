package data.sqlquery

import cats.effect.{ExitCode, IO, IOApp}
import org.http4s.Request

import scala.concurrent.ExecutionContext.Implicits.global
import doobie.*
import doobie.implicits.*
import doobie.util.ExecutionContexts
import doobie.util.fragments
import io.circe.Json
import io.circe.literal.json
import io.circe.syntax.*
import data.sqlquery.PersonQuery.UserInfo

import java.sql.{DriverManager, SQLException}
import data.{Sort, SortBy}
import routes.Session
object PostQuery{
  case class Post(id:Int, createdAt:String, title:String, description:String, year:String, imagePath:String,
                  videoPath:String, episodeCount:Int, episodeDuration:Int, userId:Int, typeId:Int, rating:Double, xxxContent:Int, genreId:Int)

  def getPostList: IO[List[Json]] =

     getPostListDB.flatMap{
      case Left(value) => IO.pure(
        value.map(elem =>
          json"""{ "id": ${elem.id},"createdAt": ${elem.createdAt}, "title": ${elem.title},
                "description": ${elem.description},"year": ${elem.year}, "imagePath": ${elem.imagePath},
                  "videoPath": ${elem.videoPath},"episodeCount": ${elem.episodeCount}, "episodeDuration":${elem.episodeDuration},
                    "userId": ${elem.userId}, "typeId": ${elem.typeId}, "rating": ${elem.rating}, "genreId": ${elem.genreId} }""")
      )
      case Right(value) => IO.pure(List(json"""{"postError": $value}"""))

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
                   "userId": ${post.userId}, "typeId": ${post.typeId}, "rating": ${post.rating}, "xxxPostContent": ${post.xxxContent}, "genreId": ${post.genreId} }"""

       case None => json"""{"postGetError": "Такого поста не существует"}"""

     }
  def getPostList(filterType:Int, filterGenre:Int, sort:Int, sortBy:Int):IO[List[Json]] =

    val xa = Transactor.fromDriverManager[IO](
      "com.mysql.cj.jdbc.Driver",
      "jdbc:mysql://127.0.0.1/animeHub",
      "root",
      "",
    )

    getSqlQueryForFilter(filterType, filterGenre, sort, sortBy)
      .query[Post]
      .to[List]
      .transact(xa)
      .map{ posts => posts.map { elem =>
        json"""{ "id": ${elem.id},"createdAt": ${elem.createdAt}, "title": ${elem.title},
                     "description": ${elem.description},"year": ${elem.year}, "imagePath": ${elem.imagePath},
                       "videoPath": ${elem.videoPath},"episodeCount": ${elem.episodeCount}, "episodeDuration":${elem.episodeDuration},
                         "userId": ${elem.userId}, "typeId": ${elem.typeId}, "rating": ${elem.rating}, "xxxPostContent": ${elem.xxxContent}, "genreId": ${elem.genreId} }"""
      }
      }
      .handleErrorWith(
        e => IO.pure( List.empty[Json] )
      )

  private def getSqlQueryForFilter(filterType:Int, filterGenre:Int, sort:Int, sortBy:Int):Fragment =

      val sortColumn = Fragment.const(Sort(sort).toString)
      val sortDirection = Fragment.const(SortBy(sortBy).toString)

      val firstPat = sql"SELECT * FROM `post`"

      val secondPat = if(filterType == 0 && filterGenre == 0)
        sql""
      else
        getWhereForQuery(filterType, filterGenre)

      val thirdPat = if(sort == 0)
        sql""
      else
        sql"ORDER BY $sortColumn $sortDirection"

      firstPat ++ secondPat ++ thirdPat

 private def getWhereForQuery(filterType:Int, filterGenre:Int):Fragment =
   if(filterType != 0 && filterGenre != 0)
     sql" WHERE type_id = $filterType AND genre_id = $filterGenre"
   else if (filterType != 0 && filterGenre == 0)
     sql"WHERE type_id = $filterType"
   else if (filterGenre != 0 && filterType == 0)
     sql"WHERE genre_id = $filterGenre"
   else sql""


  def getPostList(titleSearchValue: String): IO[List[Json]] =

    val xa = Transactor.fromDriverManager[IO](
      "com.mysql.cj.jdbc.Driver",
      "jdbc:mysql://127.0.0.1/animeHub",
      "root",
      "",
    )
    val searchTitle = titleSearchValue + "%"

    sql"SELECT * FROM post WHERE `title` LIKE $searchTitle LIMIT 10"
      .query[Post]
      .to[List]
      .transact(xa)
      .map { posts =>
        posts.map(elem =>
          json"""{ "id": ${elem.id},"createdAt": ${elem.createdAt}, "title": ${elem.title},
                    "description": ${elem.description},"year": ${elem.year}, "imagePath": ${elem.imagePath},
                      "videoPath": ${elem.videoPath},"episodeCount": ${elem.episodeCount}, "episodeDuration":${elem.episodeDuration},
                        "userId": ${elem.userId}, "typeId": ${elem.typeId}, "rating": ${elem.rating}, "xxxPostContent": ${elem.xxxContent}, "genreId": ${elem.genreId} }"""

        )
      }
      .handleErrorWith(e => IO.pure( List.empty[Json] ))


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

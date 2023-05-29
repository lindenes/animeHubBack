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
import services.PhotoService

import java.util.Base64
object PostQuery{

  val xa = Transactor.fromDriverManager[IO](
    "com.mysql.cj.jdbc.Driver",
    "jdbc:mysql://127.0.0.1/animeHub",
    "root",
    ",tkstudjplbrb"
  )
  case class Post(id:Int, createdAt:String, title:String, description:String, year:String, imagePath:String,
                  videoPath:String, episodeCount:Int, episodeDuration:Int, userId:Int, typeId:Int, rating:Double, xxxContent:Int, genreId:Int)

  case class PostPage(id: Int, createdAt: String, title: String, description: String, year: String, imagePath: String,
                  videoPath: String, episodeCount: Int, episodeDuration: Int, userId: Int, typeId: Int, rating: Double, xxxContent: Int, genreId: Int,
                      genreName:String, typeName:String, countLike:Int)
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

   sql"SELECT * FROM post LIMIT 10"
     .query[Post]
     .to[List]
     .transact(xa)
     .map(posts => Left(posts))
     .handleErrorWith(e => IO(Right("Ошибка вывода постов")))

 def getPostById(id:Int):IO[Json] =

   sql"SELECT post.id, post.created_at, post.title, post.description, post.year, post.image_path, post.video_path, post.episode_count, post.episode_duration, post.user_id, post.type_id, post.rating, post.xxx_content, post.genre_id, genre.name, `type`.name, post.people_count_like  FROM post JOIN genre ON post.genre_id = genre.id JOIN `type` ON post.type_id = `type`.id WHERE post.id = $id"
     .query[PostPage]
     .option
     .transact(xa)
     .map {
       case Some(post:PostPage) => json"""{ "id": ${post.id},"createdAt": ${post.createdAt}, "title": ${post.title},
               "description": ${post.description},"year": ${post.year}, "imagePath": ${post.imagePath},
                 "videoPath": ${post.videoPath},"episodeCount": ${post.episodeCount}, "episodeDuration":${post.episodeDuration},
                   "userId": ${post.userId}, "typeId": ${post.typeId}, "rating": ${post.rating}, "xxxPostContent": ${post.xxxContent}, "genreId": ${post.genreId},
                   "typeName": ${post.typeName}, "genreName": ${post.genreName}, "countLike": ${post.countLike} }"""

       case None => json"""{"postGetError": "Такого поста не существует"}"""

     }
     .handleErrorWith(ex => IO.pure(json"""{"exception": ${ex.getMessage}}"""))
  def getPostList(filterType:Int, filterGenre:Int, sort:Int, sortBy:Int):IO[List[Json]] =

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
        sql" ORDER BY $sortColumn $sortDirection"

      firstPat ++ secondPat ++ thirdPat

 private def getWhereForQuery(filterType:Int, filterGenre:Int):Fragment =
   if(filterType != 0 && filterGenre != 0)
     sql" WHERE type_id = $filterType AND genre_id = $filterGenre"
   else if (filterType != 0)
     sql"WHERE type_id = $filterType"
   else if (filterGenre != 0)
     sql"WHERE genre_id = $filterGenre"
   else sql""


  def getPostList(titleSearchValue: String): IO[List[Json]] =

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

  def addPost(title: String, description: String, year: String, image:String, episodeCount: Int, episodeDuration: Int,
              userId: Int, typeId: Int, xxxContent: Int, genreId: Int):IO[Json] =

    val imagePath = PhotoService.uploadPostPhoto(Base64.getDecoder.decode(image), title )
    val videoPath = ""

    sql"INSERT INTO `post` (`title`, `description`, `year`, `image_path`, `video_path`, `episode_count`, `episode_duration`, `user_id`, `type_id`, `xxx_content`, `genre_id`, `rating_count`, `people_count_like`) VALUES ($title, $description, $year, $imagePath, $videoPath, $episodeCount, $episodeDuration, $userId, $typeId, $xxxContent, $genreId,0,0)"
      .update
      .run
      .transact(xa)
      .attemptSql
      .map {
        case Right(_) => json"""{"success": "true"}"""
        case Left(e) => json"""{"success":  "false"}"""
      }

  def setRating(postId:Int, rating:Int, personId:Int):IO[Json]=

    for {
      _ <- sql"UPDATE `post` SET rating_count = rating_count + $rating, people_count_like = people_count_like + 1 WHERE id = $postId"
        .update
        .run
        .transact(xa)
        .attemptSql
      _ <- sql"INSERT INTO user_rating_post (user_id, post_id) VALUES ($personId, $postId)"
        .update
        .run
        .transact(xa)
        .attemptSql
    } yield json"""{"success": "true"}"""

  def updatePostInfo(postId:Int, Post:Post):IO[Json]=
    sql"UPDATE `post` SET title = ${Post.title}, description = ${Post.description}, year = ${Post.year}, image_path = ${Post.imagePath}, episode_count = ${Post.episodeCount}, episode_duration = ${Post.episodeDuration}, type_id = ${Post.typeId}, xxx_content = ${Post.xxxContent}, genre_id = ${Post.genreId} WHERE id = ${Post.id}"
      .update
      .run
      .transact(xa)
      .attemptSql
      .map { _ => json"""{"success": "true"}""" }
      .handleErrorWith(e => IO.pure(json"""{"success":  "false"}"""))

  def deletePost(postId:Int):IO[Json]=
    sql"DELETE FROM `post` WHERE id = $postId"
      .update
      .run
      .transact(xa)
      .attemptSql
      .map{_ => json"""{"success": "true"}"""}
      .handleErrorWith( e => IO.pure(json"""{"success":  "false"}"""))







//  def getPostListJDBCAsync(): IO[Either[Post, String]] =
//    val url = "jdbc:mysql://127.0.0.1/animeHub"
//    val username = "root"
//    val password = ",tkstudjplbrb",
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

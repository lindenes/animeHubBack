package data.sqlquery

import _root_.data.OrderBy
import _root_.data.SortBy
import cats.effect.IO
import doobie.*
import doobie.implicits.*
import io.circe.literal.json
import io.circe.Json
import java.sql.SQLException
import java.util.Base64
import services.PhotoService

object PostQuery:

  private val xa = Transactor.fromDriverManager[IO](
    "com.mysql.cj.jdbc.Driver",
    "jdbc:mysql://127.0.0.1/animeHub",
    "root",
    ",tkstudjplbrb",
  )

  case class Post(
      id: Int,
      createdAt: String,
      title: String,
      description: String,
      year: String,
      imagePath: String,
      videoPath: String,
      episodeCount: Int,
      episodeDuration: Int,
      userId: Int,
      typeId: Int,
      rating: Double,
      xxxContent: Int,
      genreId: Int,
  )

  private case class PostPage(
      id: Int,
      createdAt: String,
      title: String,
      description: String,
      year: String,
      imagePath: String,
      videoPath: String,
      episodeCount: Int,
      episodeDuration: Int,
      userId: Int,
      typeId: Int,
      rating: Option[Double],
      xxxContent: Int,
      genreId: Int,
      genreName: String,
      typeName: String,
      countLike: Int,
  )

  def getPostList: IO[List[Json]] = getPostListDB.flatMap {
    case Left(value) => IO.pure(value.map(elem =>
        json"""{ "id": ${elem.id},"createdAt": ${elem
            .createdAt}, "title": ${elem.title},
                "description": ${elem.description},"year": ${elem
            .year}, "imagePath": ${elem.imagePath},
                  "videoPath": ${elem.videoPath},"episodeCount": ${elem
            .episodeCount}, "episodeDuration":${elem.episodeDuration},
                    "userId": ${elem.userId}, "typeId": ${elem
            .typeId}, "rating": ${elem.rating}, "genreId": ${elem.genreId} }"""
      ))
    case Right(value) => IO.pure(List(json"""{"postError": $value}"""))

  }

  private def getPostListDB: IO[Either[List[Post], String]] =
    sql"SELECT * FROM post LIMIT 10"
      .query[Post]
      .to[List]
      .transact(xa)
      .map(posts => Left(posts))
      .handleErrorWith(e => IO(Right("Ошибка вывода постов")))

  def getPostById(id: Int): IO[Json] =
    sql"SELECT post.id, post.created_at, post.title, post.description, post.year, post.image_path, post.video_path, post.episode_count, post.episode_duration, post.user_id, post.type_id, post.rating, post.xxx_content, post.genre_id, genre.name, `type`.name, post.people_count_like  FROM post JOIN genre ON post.genre_id = genre.id JOIN `type` ON post.type_id = `type`.id WHERE post.id = $id"
      .query[PostPage]
      .option
      .transact(xa)
      .map {
        case Some(post: PostPage) => json"""{ "id": ${post
              .id},"createdAt": ${post.createdAt}, "title": ${post.title},
               "description": ${post.description},"year": ${post
              .year}, "imagePath": ${post.imagePath},
                 "videoPath": ${post.videoPath},"episodeCount": ${post
              .episodeCount}, "episodeDuration":${post.episodeDuration},
                   "userId": ${post
              .userId}, "typeId": ${post.typeId}, "rating": ${post
              .rating
              .getOrElse(0.0)}, "xxxPostContent": ${post
              .xxxContent}, "genreId": ${post.genreId},
                   "typeName": ${post.typeName}, "genreName": ${post
              .genreName}, "countLike": ${post.countLike} }"""

        case None => json"""{"postGetError": "Такого поста не существует"}"""

      }
      .handleErrorWith(ex => IO.pure(json"""{"exception": ${ex.getMessage}}"""))

  def getPostList(
      filterType: Int,
      filterGenre: Int,
      sort: Int,
      sortBy: Int,
  ): IO[List[Json]] = getSqlQueryForFilter(filterType, filterGenre, sort, sortBy)
    .query[Post]
    .to[List]
    .transact(xa)
    .map { posts =>
      posts.map { elem =>
        json"""{ "id": ${elem
            .id},"createdAt": ${elem.createdAt}, "title": ${elem.title},
                     "description": ${elem.description},"year": ${elem
            .year}, "imagePath": ${elem.imagePath},
                       "videoPath": ${elem.videoPath},"episodeCount": ${elem
            .episodeCount}, "episodeDuration":${elem.episodeDuration},
                         "userId": ${elem.userId}, "typeId": ${elem
            .typeId}, "rating": ${elem.rating}, "xxxPostContent": ${elem
            .xxxContent}, "genreId": ${elem.genreId} }"""
      }
    }
    .handleErrorWith(e => IO.pure(List.empty[Json]))

  private def getSqlQueryForFilter(
      filterType: Int,
      filterGenre: Int,
      sort: Int,
      sortBy: Int,
  ): Fragment =

    val sortColumn = Fragment.const(SortBy.fromOrdinal(sort).toString)
    val sortDirection = Fragment.const(OrderBy.fromOrdinal(sortBy).toString)

    val firstPat = sql"SELECT * FROM `post`"

    val secondPat =
      if filterType == 0 && filterGenre == 0 then sql""
      else getWhereForQuery(filterType, filterGenre)

    val thirdPat =
      if sort == 0 then sql"" else sql" ORDER BY $sortColumn $sortDirection"

    firstPat ++ secondPat ++ thirdPat

  private def getWhereForQuery(filterType: Int, filterGenre: Int): Fragment =
    if filterType != 0 && filterGenre != 0 then
      sql" WHERE type_id = $filterType AND genre_id = $filterGenre"
    else if filterType != 0 then sql"WHERE type_id = $filterType"
    else if filterGenre != 0 then sql"WHERE genre_id = $filterGenre"
    else sql""

  def getPostList(titleSearchValue: String): IO[List[Json]] =

    val searchTitle = titleSearchValue + "%"

    sql"SELECT * FROM post WHERE `title` LIKE $searchTitle LIMIT 10"
      .query[Post]
      .to[List]
      .transact(xa)
      .map { posts =>
        posts.map(elem =>
          json"""{ "id": ${elem.id},"createdAt": ${elem
              .createdAt}, "title": ${elem.title},
                    "description": ${elem.description},"year": ${elem
              .year}, "imagePath": ${elem.imagePath},
                      "videoPath": ${elem.videoPath},"episodeCount": ${elem
              .episodeCount}, "episodeDuration":${elem.episodeDuration},
                        "userId": ${elem.userId}, "typeId": ${elem
              .typeId}, "rating": ${elem.rating}, "xxxPostContent": ${elem
              .xxxContent}, "genreId": ${elem.genreId} }"""
        )
      }
      .handleErrorWith(e => IO.pure(List.empty[Json]))

  def addPost(
      title: String,
      description: String,
      year: String,
      image: String,
      episodeCount: Int,
      episodeDuration: Int,
      userId: Int,
      typeId: Int,
      xxxContent: Int,
      genreId: Int,
  ): IO[Json] =

    val imagePath = PhotoService
      .uploadPostPhoto(Base64.getDecoder.decode(image), title)
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

  def updatePostInfo(postId: Int, Post: Post): IO[Json] =
    sql"UPDATE `post` SET title = ${Post.title}, description = ${Post
        .description}, year = ${Post.year}, episode_count = ${Post.episodeCount}, episode_duration = ${Post
        .episodeDuration}, type_id = ${Post.typeId}, xxx_content = ${Post
        .xxxContent}, genre_id = ${Post.genreId} WHERE id = ${Post.id}"
      .update
      .run
      .transact(xa)
      .attemptSql
      .map {
        case Right(_) => json"""{"success": "true"}"""
        case Left(e) => json"""{"success":  "false"}"""
      }
      .handleErrorWith(e => IO.pure(json"""{"success":  "false"}"""))

  def deletePost(postId: Int): IO[Json] =
    sql"DELETE FROM `post` WHERE id = $postId"
      .update
      .run
      .transact(xa)
      .attemptSql
      .map(_ => json"""{"success": "true"}""")
      .handleErrorWith(e => IO.pure(json"""{"success":  "false"}"""))

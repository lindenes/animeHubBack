package services

import _root_.data.sqlquery.CommentQuery
import _root_.data.sqlquery.FiltersQuery
import _root_.data.sqlquery.PersonQuery
import _root_.data.sqlquery.PlaylistQuery
import _root_.data.sqlquery.PostQuery
import _root_.data.sqlquery.PostQuery.Post
import _root_.data.sqlquery.RatingQuery
import _root_.data.sqlquery.ReportsQuery
import _root_.data.PersonRole
import cats.effect.IO
import cats.syntax.parallel.*
import io.circe.literal.json
import io.circe.Decoder
import io.circe.Json
import org.http4s.circe.CirceEntityDecoder
import org.http4s.circe.CirceEntityEncoder
import org.http4s.Request

object ServiceList extends CirceEntityEncoder with CirceEntityDecoder:

  case class User(
      login: String,
      password: String,
      passwordRepeat: String,
      age: Option[Int],
      email: String,
      photo: String,
  )

  object User:

    given Decoder[User] = Decoder.forProduct6(
      "login",
      "password",
      "passwordRepeat",
      "age",
      "email",
      "photo",
    )(User.apply)

  val testMethod: String = "Test Working"

  def doRegistration(req: Request[IO]): IO[Json] = req
    .as[User]
    .flatMap { user =>
      val validationInfo = Validation.checkValidation(user)
      val regInfo = Registration.addNewUser(user, validationInfo._2)
      IO.pure(validationInfo._1.deepMerge(regInfo))
    }

  def doAuthorization(req: Request[IO]): IO[Json] = req
    .as[Json]
    .flatMap { json =>
      val login = json.hcursor.get[String]("login").toOption.getOrElse("")
      val password = json.hcursor.get[String]("password").toOption.getOrElse("")

      val auth = Authorization.getAuth(login, password)
      IO.pure(auth)
    }

  def getPosts: IO[Json] = PostQuery.getPostList.map(posts => Json.arr(posts*))

  def getPost(id: Int): IO[Json] = PostQuery
    .getPostById(id)
    .flatMap(jsonPost =>
      CommentQuery
        .getCommentList(id)
        .map(jsonComments =>
          json"""{"Post": $jsonPost}""".deepMerge(jsonComments)
        )
    )

  def getSortedPosts(req: Request[IO]): IO[Json] = req
    .as[Json]
    .flatMap { json =>
      val filterType = json.hcursor.get[Int]("filterType").toOption.getOrElse(0)
      val filterGenre = json
        .hcursor
        .get[Int]("filterGenre")
        .toOption
        .getOrElse(0)
      val sort = json.hcursor.get[Int]("sort").toOption.getOrElse(0)
      val sortBy = json.hcursor.get[Int]("sortBy").toOption.getOrElse(0)

      PostQuery
        .getPostList(filterType, filterGenre, sort, sortBy)
        .map(posts => Json.arr(posts*))

    }

  def getFilters: IO[Json] = FiltersQuery.getFilterList

  def findPost(req: Request[IO]): IO[Json] = req
    .as[Json]
    .flatMap { json =>
      val searchTitle = json.hcursor.get[String]("search").toOption.getOrElse("")

      PostQuery.getPostList(searchTitle).map(posts => Json.arr(posts*))
    }

  def addNewComment(req: Request[IO]): IO[Json] = req
    .as[Json]
    .flatMap { json =>
      val text = json.hcursor.get[String]("text").toOption.getOrElse("")
      val postId = json.hcursor.get[Int]("postId").toOption.getOrElse(0)
      val userId = json.hcursor.get[Int]("userId").toOption.getOrElse(0)

      CommentQuery.addComment(userId, postId, text)

    }

  def getPlaylists(req: Request[IO]): IO[Json] = req
    .as[Json]
    .flatMap { json =>
      val personId = json.hcursor.get[Int]("personId").toOption.getOrElse(0)

      PlaylistQuery
        .getPersonPlaylists(personId)
        .map(playlists => Json.arr(playlists*))
    }

  def addPlayList(req: Request[IO]): IO[Json] = req
    .as[Json]
    .flatMap { json =>
      val personId = json.hcursor.get[Int]("personId").toOption.getOrElse(0)
      val playlistTitle = json
        .hcursor
        .get[String]("title")
        .toOption
        .getOrElse("")
      val isPrivate = json.hcursor.get[Int]("isPrivate").toOption.getOrElse(0)

      PlaylistQuery.addPlaylist(personId, playlistTitle, isPrivate)
    }

  def getPlaylistsItems(req: Request[IO]): IO[Json] = req
    .as[Json]
    .flatMap { json =>
      val playlistId = json.hcursor.get[Int]("playlistId").toOption.getOrElse(0)

      PlaylistQuery.getPlayListsItems(playlistId).map(items => Json.arr(items*))
    }

  def addItemToPlaylist(req: Request[IO]): IO[Json] = req
    .as[Json]
    .flatMap { json =>
      val playlistId = json.hcursor.get[Int]("playlistId").toOption.getOrElse(0)
      val postId = json.hcursor.get[Int]("postId").toOption.getOrElse(0)

      PlaylistQuery.addItemToPlaylist(playlistId, postId)
    }

  def addNewPost(req: Request[IO]): IO[Json] = req
    .as[Json]
    .flatMap { json =>
      val title = json.hcursor.get[String]("title").toOption.getOrElse("")
      val description = json
        .hcursor
        .get[String]("description")
        .toOption
        .getOrElse("")
      val year = json.hcursor.get[String]("year").toOption.getOrElse("")
      val image = json.hcursor.get[String]("image").toOption.getOrElse("")
      val episodeCount = json
        .hcursor
        .get[Int]("episodeCount")
        .toOption
        .getOrElse(0)
      val episodeDuration = json
        .hcursor
        .get[Int]("episodeDuration")
        .toOption
        .getOrElse(0)
      val userId = json.hcursor.get[Int]("userId").toOption.getOrElse(0)
      val typeId = json.hcursor.get[Int]("typeId").toOption.getOrElse(0)
      val xxxContent = json.hcursor.get[Int]("xxxContent").toOption.getOrElse(0)
      val genreId = json.hcursor.get[Int]("genreId").toOption.getOrElse(0)

      PostQuery.addPost(
        title,
        description,
        year,
        image,
        episodeCount,
        episodeDuration,
        userId,
        typeId,
        xxxContent,
        genreId,
      )
    }

  def getUserList(req: Request[IO]): IO[Json] = req
    .as[Json]
    .flatMap { json =>
      val id = json.hcursor.get[Int]("personId").toOption.getOrElse(0)
      PersonQuery.getPersonList(id).map(userList => Json.arr(userList*))
    }

  def getRoleList(req: Request[IO]): IO[Json] = req
    .as[Json]
    .flatMap { json =>
      val id = json.hcursor.get[Int]("personId").toOption.getOrElse(0)

      PersonQuery
        .getCurrentUser(id)
        .flatMap { user =>
          if user.nonEmpty && user.get.role == 4 then

            IO.pure(Json.arr(
              PersonRole
                .values
                .map { role =>
                  json"""{"roleId": ${role
                      .ordinal}, "roleName": ${role.name}}"""
                }*
            ))
          else IO.pure(json"""{"error": "Not an admin"}""")
        }
    }

  def updatePersonRole(req: Request[IO]): IO[Json] = req
    .as[Json]
    .flatMap { json =>
      val personId = json.hcursor.get[Int]("personId").toOption.getOrElse(0)
      val roleId = json.hcursor.get[Int]("roleId").toOption.getOrElse(0)

      PersonQuery.updateRole(personId, roleId)
    }

  def getPlaylistFromPost(req: Request[IO]): IO[Json] = req
    .as[Json]
    .flatMap { json =>
      val postId = json.hcursor.get[Int]("postId").toOption.getOrElse(0)
      val personId = json.hcursor.get[Int]("personId").toOption.getOrElse(0)

      PlaylistQuery
        .getPostsPlaylists(postId, personId)
        .map(items => Json.arr(items*))
    }

  def setPostRating(req: Request[IO]): IO[Json] = req
    .as[Json]
    .flatMap { json =>
      val postId = json.hcursor.get[Int]("postId").toOption.getOrElse(0)
      val personId = json.hcursor.get[Int]("personId").toOption.getOrElse(0)
      val rating = json.hcursor.get[Int]("rating").toOption.getOrElse(0)

      RatingQuery.setRating(postId, rating, personId)
    }

  def dropPersonPlaylist(req: Request[IO]): IO[Json] = req
    .as[Json]
    .flatMap { json =>
      val playlistId = json.hcursor.get[Int]("playlistId").toOption.getOrElse(0)
      val personId = json.hcursor.get[Int]("personId").toOption.getOrElse(0)
      PlaylistQuery.dropPlaylist(playlistId, personId)
    }

  def dropPlaylistItem(req: Request[IO]): IO[Json] = req
    .as[Json]
    .flatMap { json =>
      val playlistId = json.hcursor.get[Int]("playlistId").toOption.getOrElse(0)
      val postId = json.hcursor.get[Int]("postId").toOption.getOrElse(0)
      PlaylistQuery.dropPlaylistsItem(playlistId, postId)
    }

  def getPersonComments(req: Request[IO]): IO[Json] = req
    .as[Json]
    .flatMap { json =>
      val personId = json.hcursor.get[Int]("personId").toOption.getOrElse(0)
      CommentQuery.getPersonComments(personId).map(items => Json.arr(items*))
    }

  def updatePersonXxxContent(req: Request[IO]): IO[Json] = req
    .as[Json]
    .flatMap { json =>
      val personId = json.hcursor.get[Int]("personId").toOption.getOrElse(0)
      val xxxContent = json.hcursor.get[Int]("xxxContent").toOption.getOrElse(0)

      PersonQuery.updateXxxContent(personId, xxxContent)
    }

  def deletePost(req: Request[IO]): IO[Json] = req
    .as[Json]
    .flatMap { json =>
      val postId = json.hcursor.get[Int]("postId").toOption.getOrElse(0)
      PostQuery.deletePost(postId)
    }

  def updatePost(req: Request[IO]): IO[Json] = req
    .as[Json]
    .flatMap { json =>
      val postId = json.hcursor.get[Int]("postId").toOption.getOrElse(0)
      val post = Post(
        postId,
        "",
        json.hcursor.get[String]("title").toOption.getOrElse(""),
        json.hcursor.get[String]("description").toOption.getOrElse(""),
        json.hcursor.get[String]("year").toOption.getOrElse(""),
        json.hcursor.get[String]("imagePath").toOption.getOrElse(""),
        json.hcursor.get[String]("videoPath").toOption.getOrElse(""),
        json.hcursor.get[Int]("episodeCount").toOption.getOrElse(0),
        json.hcursor.get[Int]("episodeDuration").toOption.getOrElse(0),
        json.hcursor.get[Int]("userId").toOption.getOrElse(0),
        json.hcursor.get[Int]("typeId").toOption.getOrElse(0),
        0.0,
        json.hcursor.get[Int]("xxxContent").toOption.getOrElse(0),
        json.hcursor.get[Int]("genreId").toOption.getOrElse(0),
      )
      PostQuery.updatePostInfo(postId, post)
    }

  def getPersonPostRating(req: Request[IO]): IO[Json] = req
    .as[Json]
    .flatMap { json =>
      val personId = json.hcursor.get[Int]("personId").toOption.getOrElse(0)
      val postId = json.hcursor.get[Int]("postId").toOption.getOrElse(0)

      RatingQuery.getPersonPostRating(personId, postId)
    }

  def updatePersonPostRating(req: Request[IO]): IO[Json] = req
    .as[Json]
    .flatMap { json =>
      val personId = json.hcursor.get[Int]("personId").toOption.getOrElse(0)
      val postId = json.hcursor.get[Int]("postId").toOption.getOrElse(0)
      val rating = json.hcursor.get[Int]("rating").toOption.getOrElse(0)

      RatingQuery.updatePersonPostRating(personId, postId, rating)
    }

  def parallelFunc(): IO[Json] =
    (PersonQuery.getPersonList(1), PostQuery.getPostList).parMapN { (a, b) =>
      Json.arr(a ::: b: _*)
    }

  def nonParalle(): IO[Json] =
    for
      persQ <- PersonQuery.getPersonList(1)
      postList <- PostQuery.getPostList
    yield Json.arr(persQ ::: postList: _*)

  def addReport(req: Request[IO]): IO[Json] = req
    .as[Json]
    .flatMap { json =>
      val commentId = json.hcursor.get[Int]("commentId").toOption.getOrElse(0)

      ReportsQuery.addReport(commentId)
    }

  def getReportList(req: Request[IO]): IO[Json] = req
    .as[Json]
    .flatMap { json =>
      val personId = json.hcursor.get[Int]("personId").toOption.getOrElse(0)
      ReportsQuery.getReportList(personId).map(items => Json.arr(items*))
    }

  def delReportComment(req: Request[IO]): IO[Json] = req
    .as[Json]
    .flatMap { json =>
      val commentId = json.hcursor.get[Int]("commentId").toOption.getOrElse(0)
      val reportId = json.hcursor.get[Int]("reportId").toOption.getOrElse(0)

      (CommentQuery.delComment(commentId), ReportsQuery.delReport(reportId))
        .parMapN { (a, b) =>
          if a && b then json"""{"success": "true"}"""
          else json"""{"success": "false"}"""
        }
    }

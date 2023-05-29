package services

import cats.Applicative
import cats.effect.{IO, Sync}
import data.PersonRole
import io.circe.*
import io.circe.literal.*
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.circe.jsonOf
import org.http4s.{Request, UrlForm}
import services.Validation
import data.sqlquery.{CommentQuery, FiltersQuery, PersonQuery, PlaylistQuery, PostQuery}
import io.circe.syntax.*
import data.sqlquery.PostQuery.Post
object ServiceList {
  case class User(login: String, password: String, passwordRepeat: String, age:Option[Int], email:String, photo:String)

  val validation = new Validation()
  def testMethod(): String = "Test Working"

  def doRegistration(req: Request[IO]): IO[Json] =
    req.as[Json].flatMap { json =>
      val login = json.hcursor.get[String]("login").toOption.getOrElse("")
      val password = json.hcursor.get[String]("password").toOption.getOrElse("")
      val passwordRepeat = json.hcursor.get[String]("passwordRepeat").toOption.getOrElse("")
      val email = json.hcursor.get[String]("email").toOption.getOrElse("")
      val age = json.hcursor.get[Int]("age").toOption.getOrElse(0)
      val photo = json.hcursor.get[String]("photo").toOption.getOrElse("")

      val user = User(login, password, passwordRepeat, Some(age), email, photo)

      val validationInfo = validation.checkValidation(user)
      val regInfo = Registration.addNewUser(user, validationInfo._2)
      IO.pure(
        validationInfo._1.deepMerge(regInfo)
      )
    }

  def doAuthorization(req: Request[IO] ): IO[Json] =
    req.as[Json].flatMap { json =>
      val login = json.hcursor.get[String]("login").toOption.getOrElse("")
      val password = json.hcursor.get[String]("password").toOption.getOrElse("") 
      
      val auth = Authorization.getAuth(login, password)
      IO.pure(
        auth
      )
    }
  def getPosts: IO[Json] = PostQuery.getPostList.map(posts => Json.arr(posts: _*))

  def getPost(id:Int): IO[Json] = PostQuery.getPostById(id).flatMap( jsonPost =>
    CommentQuery.getCommentList(id).map(jsonComments =>
      json"""{"Post": $jsonPost}""".deepMerge(jsonComments)
    )
  )

  def getSortedPosts(req: Request[IO] ):IO[Json]=
    req.as[Json].flatMap{ json =>
      val filterType = json.hcursor.get[Int]("filterType").toOption.getOrElse(0)
      val filterGenre = json.hcursor.get[Int]("filterGenre").toOption.getOrElse(0)
      val sort = json.hcursor.get[Int]("sort").toOption.getOrElse(0)
      val sortBy = json.hcursor.get[Int]("sortBy").toOption.getOrElse(0)

      PostQuery.getPostList(filterType, filterGenre, sort ,sortBy).map(posts => Json.arr(posts: _*))

    }
  def getFilters:IO[Json] = FiltersQuery.getFilterList

  def findPost(req: Request[IO] ): IO[Json] =
    req.as[Json].flatMap{ json =>
      val searchTitle = json.hcursor.get[String]("search").toOption.getOrElse("")
      
      PostQuery.getPostList(searchTitle).map(posts => Json.arr(posts: _*))
    }

  def addNewComment(req:Request[IO]): IO[Json]=
    req.as[Json].flatMap{ json =>
      val text = json.hcursor.get[String]("text").toOption.getOrElse("")
      val postId = json.hcursor.get[Int]("postId").toOption.getOrElse(0)
      val userId = json.hcursor.get[Int]("userId").toOption.getOrElse(0)
      
      CommentQuery.addComment(userId, postId, text)

    }

  def getPlaylists(req:Request[IO]):IO[Json]=
    req.as[Json].flatMap{json =>
      val personId = json.hcursor.get[Int]("personId").toOption.getOrElse(0)

      PlaylistQuery.getPersonPlaylists(personId).map(playlists => Json.arr(playlists: _*))
    }

  def addPlayList(req:Request[IO]):IO[Json]=
    req.as[Json].flatMap{json=>
      val personId = json.hcursor.get[Int]("personId").toOption.getOrElse(0)
      val playlistTitle = json.hcursor.get[String]("title").toOption.getOrElse("")
      val isPrivate = json.hcursor.get[Int]("isPrivate").toOption.getOrElse(0)

      PlaylistQuery.addPlaylist(personId, playlistTitle, isPrivate)
    }

  def getPlaylistsItems(req:Request[IO]):IO[Json] =
    req.as[Json].flatMap{ json =>
      val playlistId = json.hcursor.get[Int]("playlistId").toOption.getOrElse(0)

      PlaylistQuery.getPlayListsItems(playlistId).map(items => Json.arr(items:_*))
    }

  def addItemToPlaylist(req:Request[IO]):IO[Json] =
    req.as[Json].flatMap{ json =>
      val playlistId = json.hcursor.get[Int]("playlistId").toOption.getOrElse(0)
      val  postId = json.hcursor.get[Int]("postId").toOption.getOrElse(0)

      PlaylistQuery.addItemToPlaylist(playlistId, postId)
    }

  def addNewPost(req:Request[IO]):IO[Json] =
    req.as[Json].flatMap{ json =>
      val title = json.hcursor.get[String]("title").toOption.getOrElse("")
      val description = json.hcursor.get[String]("description").toOption.getOrElse("")
      val year = json.hcursor.get[String]("year").toOption.getOrElse("")
      val image = json.hcursor.get[String]("image").toOption.getOrElse("")
      val episodeCount = json.hcursor.get[Int]("episodeCount").toOption.getOrElse(0)
      val episodeDuration = json.hcursor.get[Int]("episodeDuration").toOption.getOrElse(0)
      val userId = json.hcursor.get[Int]("userId").toOption.getOrElse(0)
      val typeId = json.hcursor.get[Int]("typeId").toOption.getOrElse(0)
      val xxxContent = json.hcursor.get[Int]("xxxContent").toOption.getOrElse(0)
      val genreId = json.hcursor.get[Int]("genreId").toOption.getOrElse(0)
      
      PostQuery.addPost(title, description, year, image, episodeCount, episodeDuration, userId, typeId, xxxContent, genreId)
    }

  def getUserList(req:Request[IO]):IO[Json]=
    req.as[Json].flatMap{ json =>
      val id = json.hcursor.get[Int]("personId").toOption.getOrElse(0)
      PersonQuery.getPersonList(id).map(userList => Json.arr(userList: _*))
    }
  def getRoleList(req:Request[IO]):IO[Json] =
    req.as[Json].flatMap{ json =>
      val id = json.hcursor.get[Int]("personId").toOption.getOrElse(0)

       PersonQuery.getCurrentUser(id).flatMap{ user =>
         if user.nonEmpty && user.get.role == 4 then

           IO.pure(
             Json.arr(
             PersonRole.roleList.map{ role => json"""{"roleId": ${role.id}, "roleName": ${role.toString}}""" }: _*)
           )

         else
           IO.pure(json"""{"error": "Вы не администратор, досвидание"}""")
       }
    }
    
  def updatePersonRole(req:Request[IO]):IO[Json]=
    req.as[Json].flatMap{ json =>
      val personId = json.hcursor.get[Int]("personId").toOption.getOrElse(0)
      val roleId = json.hcursor.get[Int]("roleId").toOption.getOrElse(0)
      
      PersonQuery.updateRole(personId, roleId)
    }

  def getPlaylistFromPost(req:Request[IO]):IO[Json]=
    req.as[Json].flatMap{json=>
      val postId = json.hcursor.get[Int]("postId").toOption.getOrElse(0)
      val personId = json.hcursor.get[Int]("personId").toOption.getOrElse(0)

      PlaylistQuery.getPostsPlaylists(postId, personId).map(items => Json.arr(items:_*))
    }

  def setPostRating(req:Request[IO]):IO[Json]=
    req.as[Json].flatMap{ json =>
      val postId = json.hcursor.get[Int]("postId").toOption.getOrElse(0)
      val personId = json.hcursor.get[Int]("personId").toOption.getOrElse(0)
      val rating = json.hcursor.get[Int]("rating").toOption.getOrElse(0)

      PostQuery.setRating(postId, rating, personId)
    }
    
  def dropPersonPlaylist(req:Request[IO]):IO[Json] =
    req.as[Json].flatMap{ json =>
      val playlistId = json.hcursor.get[Int]("playlistId").toOption.getOrElse(0)
      val personId = json.hcursor.get[Int]("personId").toOption.getOrElse(0)
      PlaylistQuery.dropPlaylist(playlistId, personId)
    }
    
  def dropPlaylistItem(req:Request[IO]):IO[Json]=
    req.as[Json].flatMap{ json =>
      val playlistId = json.hcursor.get[Int]("playlistId").toOption.getOrElse(0)
      val postId = json.hcursor.get[Int]("postId").toOption.getOrElse(0)
      PlaylistQuery.dropPlaylistsItem(playlistId, postId)
    }
    
  def getPersonComments(req:Request[IO]):IO[Json]=
    req.as[Json].flatMap{ json =>
      val personId = json.hcursor.get[Int]("personId").toOption.getOrElse(0)
      CommentQuery.getPersonComments(personId).map(items => Json.arr(items:_*))
    }
    
  def updatePersonXxxContent(req:Request[IO]):IO[Json]=
    req.as[Json].flatMap { json =>
      val personId = json.hcursor.get[Int]("personId").toOption.getOrElse(0)
      val xxxContent = json.hcursor.get[Int]("xxxContent").toOption.getOrElse(0)
      
      PersonQuery.updateXxxContent(personId, xxxContent)
    }

  def deletePost(req:Request[IO]):IO[Json]=
    req.as[Json].flatMap{ json =>
      val postId = json.hcursor.get[Int]("postId").toOption.getOrElse(0)
      PostQuery.deletePost(postId)
    }

  def updatePost(req:Request[IO]):IO[Json]=
    req.as[Json].flatMap{ json =>
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
}



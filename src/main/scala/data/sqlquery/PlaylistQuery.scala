package data.sqlquery

import cats.effect.{ExitCode, IO, IOApp}
import doobie.*
import doobie.implicits.*
import doobie.util.ExecutionContexts
import doobie.util.fragments
import io.circe.Json
import io.circe.literal.json
import io.circe.syntax.*
import PostQuery.Post
object PlaylistQuery {

  case class  Playlist(id:Int, created_at:String, title:String)

  def getPersonPlaylists(personId: Int):IO[List[Json]] =

    val xa = Transactor.fromDriverManager[IO](
      "com.mysql.cj.jdbc.Driver",
      "jdbc:mysql://127.0.0.1/animeHub",
      "root",
      ",tkstudjplbrb",
    )

    sql"SELECT `id`, `created_at`, `title` FROM `playlist` WHERE user_id = $personId"
      .query[Playlist]
      .to[List]
      .transact(xa)
      .map{ playlists =>
        playlists.map{
          elem => json"""{"playlistId":  ${elem.id}, "createdAt":  ${elem.created_at}, "title": ${elem.title}}"""
        }

      }
      .handleErrorWith( ex => IO.pure( List.empty[Json] ))

  def addPlaylist(personId:Int, playlistTitle:String, isPrivate:Int):IO[Json]=

    val xa = Transactor.fromDriverManager[IO](
      "com.mysql.cj.jdbc.Driver",
      "jdbc:mysql://127.0.0.1/animeHub",
      "root",
      ",tkstudjplbrb",
    )

    sql"INSERT INTO `playlist` (`title`, `user_id`, `is_private`) VALUES ($playlistTitle, $personId, $isPrivate)"
      .update
      .run
      .transact(xa)
      .attemptSql
      .map {
        case Right(_) => json"""{"success": "true"}"""
        case Left(e) => json"""{"success":  "false"}"""
      }

  def addItemToPlaylist(playlistId:Int, postId:Int ):IO[Json]=

    val xa = Transactor.fromDriverManager[IO](
      "com.mysql.cj.jdbc.Driver",
      "jdbc:mysql://127.0.0.1/animeHub",
      "root",
      ",tkstudjplbrb",
    )

    sql"INSERT INTO `post_playlist` (`playlist_id`, `post_id`) VALUES ($playlistId, $postId)"
      .update
      .run
      .transact(xa)
      .attemptSql
      .map {
        case Right(_) => json"""{"success": "true"}"""
        case Left(e) => json"""{"success":  "false"}"""
      }

  def getPlayListsItems(playlistId:Int):IO[List[Json]] =

    val xa = Transactor.fromDriverManager[IO](
      "com.mysql.cj.jdbc.Driver",
      "jdbc:mysql://127.0.0.1/animeHub",
      "root",
      ",tkstudjplbrb",
    )

    sql"SELECT * FROM `post` WHERE id IN (SELECT post_id FROM `post_playlist` WHERE playlist_id = $playlistId)"
      .query[Post]
      .to[List]
      .transact(xa)
      .map{ posts =>
        posts.map{
          elem =>
            json"""{ "id": ${elem.id},"createdAt": ${elem.createdAt}, "title": ${elem.title},
                  "description": ${elem.description},"year": ${elem.year}, "imagePath": ${elem.imagePath},
                    "videoPath": ${elem.videoPath},"episodeCount": ${elem.episodeCount}, "episodeDuration":${elem.episodeDuration},
                      "userId": ${elem.userId}, "typeId": ${elem.typeId}, "rating": ${elem.rating}, "genreId": ${elem.genreId} }"""
          
        }
        
      }
      .handleErrorWith( ex => IO.pure( List.empty[Json] ))

}

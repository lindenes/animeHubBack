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
import data.sqlquery.PersonQuery.UserInfo
object PlaylistQuery {

  val xa = Transactor.fromDriverManager[IO](
    "com.mysql.cj.jdbc.Driver",
    "jdbc:mysql://127.0.0.1/animeHub",
    "root",
    ",tkstudjplbrb"
  )

  case class  Playlist(id:Int, created_at:String, title:String)

  def getPersonPlaylists(personId: Int):IO[List[Json]] =

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
                                     "userId": ${elem.userId}, "typeId": ${elem.typeId}, "rating": ${elem.rating}, "xxxPostContent": ${elem.xxxContent}, "genreId": ${elem.genreId} }"""
          
        }
        
      }
      .handleErrorWith( ex => IO.pure( List.empty[Json] ))

  def getPostsPlaylists(postId:Int, personId: Int):IO[List[Json]] =

    sql"SELECT * FROM `playlist` WHERE id IN(SELECT playlist_id FROM `post_playlist` WHERE post_id = $postId) AND user_id = $personId"
      .query[Playlist]
      .to[List]
      .transact(xa)
      .map { playlists =>
        playlists.map {
          elem => json"""{"playlistId":  ${elem.id}, "createdAt":  ${elem.created_at}, "title": ${elem.title}}"""
        }

      }
      .handleErrorWith(ex => IO.pure(List.empty[Json]))

  def dropPlaylist(playlistId:Int, personId: Int):IO[Json] =

    for {
      _ <- sql"DELETE FROM `playlist` WHERE id = $playlistId AND user_id = $personId"
        .update
        .run
        .transact(xa)
        .attemptSql
      _ <- sql"DELETE FROM `post_playlist` WHERE playlist_id = $playlistId"
        .update
        .run
        .transact(xa)
        .attemptSql
    } yield json"""{"success": "true"}"""

  def dropPlaylistsItem(playlistId:Int, postId:Int):IO[Json]=

    sql"DELETE FROM `post_playlist` WHERE playlist_id = $playlistId AND post_id = $postId"
      .update
      .run
      .transact(xa)
      .attemptSql
      .map {
        case Right(_) => json"""{"success": "true"}"""
        case Left(e) => json"""{"success":  "false"}"""
      }

}

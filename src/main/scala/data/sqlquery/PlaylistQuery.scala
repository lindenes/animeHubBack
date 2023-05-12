package data.sqlquery

import cats.effect.{ExitCode, IO, IOApp}
import doobie.*
import doobie.implicits.*
import doobie.util.ExecutionContexts
import doobie.util.fragments
import io.circe.Json
import io.circe.literal.json
import io.circe.syntax.*
object PlaylistQuery {

  case class  Playlist(id:Int, created_at:String, title:String)

  def getPersonPlaylists(personId: Int):IO[List[Json]] =

    val xa = Transactor.fromDriverManager[IO](
      "com.mysql.cj.jdbc.Driver",
      "jdbc:mysql://127.0.0.1/animeHub",
      "root",
      ""
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

  def addPlaylist(personId:Int, playlistTitle:String):IO[Json]=

    val xa = Transactor.fromDriverManager[IO](
      "com.mysql.cj.jdbc.Driver",
      "jdbc:mysql://127.0.0.1/animeHub",
      "root",
      ""
    )

    sql"INSERT INTO `playlist` (`title`, `user_id`) VALUES ($playlistTitle, $personId)"
      .update
      .run
      .transact(xa)
      .attemptSql
      .map {
        case Right(_) => json"""{"success": "true"}"""
        case Left(e) => json"""{"success":  "false"}"""
      }
}

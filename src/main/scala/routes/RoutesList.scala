package routes

import cats.effect.*
import org.http4s.*
import org.http4s.dsl.Http4sDsl
import services.ServiceList
import org.http4s.circe.*
import io.circe.*
import io.circe.literal.*
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.io.*
import org.http4s.headers.Cookie
import cats.implicits.*
import cats.syntax.foldable.*
import data.sqlquery.PersonQuery.UserInfo
import org.typelevel.ci.CIString
object RoutesList {

  def getRouteList:HttpRoutes[IO] =

    val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
      case GET -> Root / "test" =>
        Ok(ServiceList.testMethod())
      case req@POST -> Root / "signup" =>
        Ok( ServiceList.doRegistration(req) )
      case req@POST -> Root / "login" =>
        Ok ( ServiceList.doAuthorization(req) ).map(_.putHeaders(Header.Raw(CIString("Set-Cookie"), s"authcookie=${Session.get("currentUser").getOrElse(0).toString}; Path=/")))
      case GET -> Root / "posts" =>
        Ok ( ServiceList.getPosts )
      case GET -> Root / "post" / IntVar(id) =>
        Ok( ServiceList.getPost(id) )
      case req@POST -> Root / "sortedPosts" =>
        Ok( ServiceList.getSortedPosts(req) )
      case GET -> Root / "filters" =>
        Ok(ServiceList.getFilters)
      case req@POST -> Root / "search" =>
        Ok( ServiceList.findPost(req) )
      case req@POST -> Root / "commentAdd" =>
        Ok( ServiceList.addNewComment(req) )
      case req@POST -> Root / "playlists" =>
        Ok( ServiceList.getPlaylists(req) )
      case req@POST -> Root / "addPlaylist" =>
        Ok( ServiceList.addPlayList(req) )
      case req@POST -> Root / "getPlaylistsItems" =>
        Ok( ServiceList.getPlaylistsItems(req) )
      case req@POST -> Root / "addPlaylistItem" =>
        Ok( ServiceList.addItemToPlaylist(req) )
    }
    routes
}

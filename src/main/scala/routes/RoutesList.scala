package routes

import _root_.services.ServiceList
import cats.effect.*
import cats.implicits.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.io.*

object RoutesList:

  def getRouteList: HttpRoutes[IO] =

    val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
      case GET -> Root / "test" => Ok(ServiceList.testMethod)
      case GET -> Root / "posts" => Ok(ServiceList.getPosts)
      case GET -> Root / "post" / IntVar(id) => Ok(ServiceList.getPost(id))
      case req @ POST -> Root / "sortedPosts" =>
        Ok(ServiceList.getSortedPosts(req))
      case GET -> Root / "filters" => Ok(ServiceList.getFilters)
      case req @ POST -> Root / "search" => Ok(ServiceList.findPost(req))
      case req @ POST -> Root / "commentAdd" =>
        Ok(ServiceList.addNewComment(req))
      case req @ POST -> Root / "playlists" => Ok(ServiceList.getPlaylists(req))
      case req @ POST -> Root / "addPlaylist" => Ok(ServiceList.addPlayList(req))
      case req @ POST -> Root / "getPlaylistsItems" =>
        Ok(ServiceList.getPlaylistsItems(req))
      case req @ POST -> Root / "addPlaylistItem" =>
        Ok(ServiceList.addItemToPlaylist(req))
      case req @ POST -> Root / "addPost" => Ok(ServiceList.addNewPost(req))
      case req @ POST -> Root / "userList" => Ok(ServiceList.getUserList(req))
      case req @ POST -> Root / "roleList" => Ok(ServiceList.getRoleList(req))
      case req @ POST -> Root / "postsPlaylists" =>
        Ok(ServiceList.getPlaylistFromPost(req))
      case req @ POST -> Root / "setRating" => Ok(ServiceList.setPostRating(req))
      case req @ POST -> Root / "deletePost" => Ok(ServiceList.deletePost(req))
      case req @ POST -> Root / "updatePost" => Ok(ServiceList.updatePost(req))
      case GET -> Root / "parall" => Ok(ServiceList.parallelFunc())
      case GET -> Root / "nonParall" => Ok(ServiceList.nonParalle())
      case req @ POST -> Root / "addReport" => Ok(ServiceList.addReport(req))
      case req @ POST -> Root / "getReports" =>
        Ok(ServiceList.getReportList(req))
      case req @ POST -> Root / "delReportComment" =>
        Ok(ServiceList.delReportComment(req))
    }

    val personRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] {
      case req @ POST -> Root / "signup" => Ok(ServiceList.doRegistration(req))
      case req @ POST -> Root / "login" => Ok(ServiceList.doAuthorization(req))
      case req @ POST -> Root / "getPostPersonRating" =>
        Ok(ServiceList.getPersonPostRating(req))
      case req @ POST -> Root / "updatePersonPostRating" =>
        Ok(ServiceList.updatePersonPostRating(req))
      case req @ POST -> Root / "updateRole" =>
        Ok(ServiceList.updatePersonRole(req))
      case req @ POST -> Root / "dropPlaylist" =>
        Ok(ServiceList.dropPersonPlaylist(req))
      case req @ POST -> Root / "dropPlaylistItem" =>
        Ok(ServiceList.dropPlaylistItem(req))
      case req @ POST -> Root / "getPersonComment" =>
        Ok(ServiceList.getPersonComments(req))
      case req @ POST -> Root / "updateXXX" =>
        Ok(ServiceList.updatePersonXxxContent(req))
    }

    routes <+> personRoutes

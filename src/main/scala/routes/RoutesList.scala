package routes

import cats.effect.*
import org.http4s.*
import org.http4s.dsl.Http4sDsl
import services.ServiceList
import org.http4s.circe._
import io.circe._
import io.circe.literal._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.io._
import cats.implicits._
object RoutesList {

  def getRouteList:HttpRoutes[IO] =

    val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
      case GET -> Root / "test" =>
        Ok(ServiceList.testMethod())
      case req@POST -> Root / "signup" =>
        Ok( ServiceList.doRegistration(req) )
      case req@POST -> Root / "login" =>
        Ok ( ServiceList.doAuthorization(req) )
      case GET -> Root / "posts" =>
        Ok ( ServiceList.getPosts )
      case GET -> Root / "post" / IntVar(id) =>
        Ok( ServiceList.getPost(id) )
    }
    routes
}

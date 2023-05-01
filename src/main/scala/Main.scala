import cats.effect.IOApp
import cats.effect.*
import com.comcast.ip4s.*
import org.http4s.{HttpRoutes, Uri}
import org.http4s.dsl.io.*
import org.http4s.implicits.*
import org.http4s.ember.server.*
import org.http4s.server.middleware.{CORS, CORSPolicy}
import org.http4s.headers.Origin
import services.ServiceList
import routes.RoutesList
object Main extends IOApp {

  val cors: CORSPolicy = CORS.policy.withAllowOriginHost(Set(
    Origin.Host(Uri.Scheme.http, Uri.RegName("10.0.0.33"), Some(3000)),
    Origin.Host(Uri.Scheme.http, Uri.RegName("localhost"), Some(3000))
  ))
    .withAllowCredentials(false)

  val cors2: CORSPolicy = CORS.policy.withAllowOriginAll
    .withAllowCredentials(false)

  def run(args: List[String]): IO[ExitCode] =
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"5000")
      .withHttpApp( cors (RoutesList.getRouteList.orNotFound) )
      .build
      .use(_ => IO.never)
      .as(ExitCode.Success)

}

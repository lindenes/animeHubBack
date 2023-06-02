import _root_.routes.RoutesList
import cats.effect.*
import com.comcast.ip4s.*
import org.http4s.ember.server.*
import org.http4s.implicits.*
import org.http4s.server.middleware.CORS
import org.http4s.server.middleware.CORSPolicy

object Main extends IOApp:

  private val cors2: CORSPolicy = CORS
    .policy
    .withAllowOriginAll
    .withAllowCredentials(false)

  def run(args: List[String]): IO[ExitCode] = EmberServerBuilder
    .default[IO]
    .withHost(ipv4"0.0.0.0")
    .withPort(port"5000")
    .withHttpApp(cors2(RoutesList.getRouteList.orNotFound))
    .build
    .use(_ => IO.never)
    .as(ExitCode.Success)

package services

import cats.Applicative
import cats.effect.{IO, Sync}
import io.circe.*
import io.circe.generic.semiauto.deriveDecoder
import io.circe.literal.*
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.circe.jsonOf
import org.http4s.{Request, UrlForm}
import services.Validation
object ServiceList {
  case class User(login: String, password: String, passwordRepeat: String)

  implicit val decoder: Decoder[User] = deriveDecoder[User]

  val validation = new Validation()

  def testMethod(): String = "Test Working"

  def registration(req: Request[IO]): IO[Json] =
    req.as[User].flatMap { user =>
      val valid = validation.checkValidation(user)
      val regMessage = validation.doRegistration(user)
        IO.pure(
          json"""{"passwordError": ${valid.passwordError}, "loginError": ${valid.loginError}}""".deepMerge(regMessage)

        )
    }
}



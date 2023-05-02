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
  case class User(login: String, password: String, passwordRepeat: String, age:Option[Int], email:String)

  val validation = new Validation()
  def testMethod(): String = "Test Working"

  def doRegistration(req: Request[IO]): IO[Json] =
    req.as[Json].flatMap { json =>
      val login = json.hcursor.get[String]("login").toOption.getOrElse("")
      val password = json.hcursor.get[String]("password").toOption.getOrElse("")
      val passwordRepeat = json.hcursor.get[String]("passwordRepeat").toOption.getOrElse("")
      val email = json.hcursor.get[String]("email").toOption.getOrElse("")
      val age = json.hcursor.get[Int]("age").toOption.getOrElse(0)
      val user = User(login, password, passwordRepeat, Some(age), email)

      val validationInfo = validation.checkValidation(user)
      val regInfo = Registration.addNewUser(user, validationInfo._2)
      IO.pure(
        json"""{"passwordError": ${validationInfo._1.passwordError}, "loginError": ${validationInfo._1.loginError}, 
              "mailError": ${validationInfo._1.mailError}, "ageError":  ${validationInfo._1.ageError}}""".deepMerge(regInfo))
    }
}



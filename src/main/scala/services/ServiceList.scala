package services

import cats.Applicative
import cats.effect.{IO, Sync}
import io.circe.*
import io.circe.literal.*
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.circe.jsonOf
import org.http4s.{Request, UrlForm}
import services.Validation
import data.sqlquery.{PersonQuery, PostQuery}
import io.circe.syntax._
object ServiceList {
  case class User(login: String, password: String, passwordRepeat: String, age:Option[Int], email:String, photoPath:String)

  val validation = new Validation()
  def testMethod(): String = "Test Working"

  def doRegistration(req: Request[IO]): IO[Json] =
    req.as[Json].flatMap { json =>
      val login = json.hcursor.get[String]("login").toOption.getOrElse("")
      val password = json.hcursor.get[String]("password").toOption.getOrElse("")
      val passwordRepeat = json.hcursor.get[String]("passwordRepeat").toOption.getOrElse("")
      val email = json.hcursor.get[String]("email").toOption.getOrElse("")
      val age = json.hcursor.get[Int]("age").toOption.getOrElse(0)

      val photoBytes = json.hcursor.get[Array[Byte]]("photo").getOrElse(Array.emptyByteArray)
      val photoPath = PhotoService.uploadPhoto(photoBytes)

      val user = User(login, password, passwordRepeat, Some(age), email, photoPath)

      val validationInfo = validation.checkValidation(user)
      val regInfo = Registration.addNewUser(user, validationInfo._2)
      IO.pure(
        validationInfo._1.deepMerge(regInfo)
      )
    }

  def doAuthorization(req: Request[IO] ): IO[Json] =
    req.as[Json].flatMap { json =>
      val login = json.hcursor.get[String]("login").toOption.getOrElse("")
      val password = json.hcursor.get[String]("password").toOption.getOrElse("") 
      
      val auth = Authorization.getAuth(login, password)
      IO.pure(
        auth
      )
    }
  def getPosts: IO[Json] = PostQuery.getPostList
    
}



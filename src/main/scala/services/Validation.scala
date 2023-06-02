package services

import cats.data.Validated
import cats.data.ValidatedNec
import cats.syntax.all.*
import cats.syntax.validated.*
import io.circe.syntax.*
import io.circe.Encoder
import io.circe.Json
import services.ServiceList.User

object Validation:

  private case class ValidationInfo(
      passwordError: List[String],
      loginError: List[String],
      ageError: List[String],
      mailError: List[String],
  )

  private object ValidationInfo:

    val empty: ValidationInfo =
      ValidationInfo(List.empty, List.empty, List.empty, List.empty)

    given Encoder[ValidationInfo] = Encoder
      .forProduct4("passwordError", "loginError", "ageError", "mailError")(
        validationInfo =>
          (
            validationInfo.passwordError,
            validationInfo.loginError,
            validationInfo.ageError,
            validationInfo.mailError,
          )
      )

  def checkValidation(user: User): (Json, Boolean) =

    val passwordCheck = passwordChecker(user)
    val loginCheck = loginChecker(user)
    val ageCheck = ageChecker(user)
    val mailCheck = mailChecker(user)

    val validation = ValidationInfo(
      passwordCheck.fold(_.toList, _ => List.empty),
      loginCheck.fold(_.toList, _ => List.empty),
      ageCheck.fold(_.toList, _ => List.empty),
      mailCheck.fold(_.toList, _ => List.empty),
    )

    val successful = (passwordCheck, loginCheck, ageCheck, mailCheck)
      .mapN((_, _, _, _) => ())
      .isValid

    (validation.asJson, successful)

  private def passwordChecker(user: User): ValidatedNec[String, Unit] = (
    if user.password != user.passwordRepeat then
      "Passwords are not equals".invalidNec
    else ().validNec,
    if user.password == "" || user.password.isEmpty then
      "Empty value".invalidNec
    else ().validNec,
    if user.password.length < 7 then
      "Password must be longer than 7 characters".invalidNec
    else ().validNec,
  ).mapN((_, _, _) => ())

  private def loginChecker(user: User): ValidatedNec[String, Unit] =
    if user.login.length < 6 then
      "Login must be longer than 6 characters".invalidNec
    else ().validNec

  private def ageChecker(user: User): ValidatedNec[String, Unit] =
    if user.age.getOrElse(0) >= 100 || user.age.getOrElse(0) <= 7 then
      "Age must be in between 7 and 100".invalidNec
    else ().validNec

  private def mailChecker(user: User): ValidatedNec[String, Unit] =
    val emailRegex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$".r
    emailRegex.findFirstMatchIn(user.email) match
      case Some(_) => ().validNec
      case None => "Incorrect email".invalidNec

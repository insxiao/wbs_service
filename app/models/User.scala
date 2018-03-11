package models

import java.sql.Date



import User.Gender
case class User(id: Option[Long],
                name: String,
                gender: Gender,
                password: String,
                email: Option[String],
                birthday: Option[Date]) {
  def changeName(newName: String): User = copy(name = newName)

  def changePassword(newPassword: String): User = copy(password = newPassword)

  def changeEmail(newEmail: Option[String] = None): User = copy(email = newEmail)

  def changeBirthday(newBirthday: Option[Date] = None): User = copy(birthday = newBirthday)
}

object User {
  import play.api.libs.json._
  implicit val userFormat: Format[User] = Json.format[User]


  abstract class Gender(val value: String)

  case object Male extends Gender("M")

  case object Female extends Gender("F")

  object Gender {

    import play.api.libs.json._

    def apply(v: String): Gender = if (v == "M") Male else Female

    implicit val GenderFormat: Format[Gender] = new Format[Gender] {
      override def reads(json: JsValue): JsResult[Gender] =
        json.validate[String] match {
          case JsSuccess("M", _) => JsSuccess(Male)
          case JsSuccess("F", _) => JsSuccess(Male)
        }

      override def writes(o: Gender): JsValue = o match {
        case Male => JsString("M")
        case Female => JsString("F")
      }
    }
  }
}









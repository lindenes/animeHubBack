package data

object PersonRole extends Enumeration{
  type PersonRole = Value

  val Unknown = Value(0, "Неизвестно")
  val Guest = Value(1, "Гость")
  val simpleUser = Value(2, "Пользователь")
  val Moderator = Value(3, "Модератор")
  val Admin = Value(4, "Администратор")

  def roleList = List(Unknown, Guest, simpleUser, Moderator, Admin)
}


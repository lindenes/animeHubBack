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
object XXXContent extends Enumeration{
  type XXXContent = Value

  val notXXX = Value(0, "Нет контента 18+")
  val yesXXX = Value(1, "Есть контент 18+")

  def XXXTypeList = List(notXXX, yesXXX)
}

object Sort extends Enumeration {
  type Sort = Value

  val default = Value (0, "")
  val date = Value(1, "created_at")
  val episodeCount = Value(2, "episode_count")
  val title = Value(3, "title")
  val rating = Value(4, "rating")
  val year = Value (5, "year")

  def sortList = List(default, date, episodeCount, title, rating, year)
}
object SortBy extends Enumeration{
  type SortBy = Value

  val byAsk = Value (0, "ASC")
  val byDesk = Value(1, "DESC")

  def SortByList = List(byAsk, byDesk)
}
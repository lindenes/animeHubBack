package data

enum PersonRole(val roleName: String):
  case Unknown extends PersonRole("Неизвестно")
  case Guest extends PersonRole("Гость")
  case SimpleUser extends PersonRole("Пользователь")
  case Moderator extends PersonRole("Модератор")
  case Admin extends PersonRole("Администратор")

enum XXXContent(val description:String):

  case notXXX extends XXXContent("Нет контента 18+")
  case yesXXX extends XXXContent("Есть контент 18+")
enum SortBy(val field: String):

  case Default extends SortBy("")
  case Year extends SortBy("year")
  case EpisodeCount extends SortBy("episode_count")
  case Title extends SortBy("title")
  case Rating extends SortBy("rating")

enum OrderBy(val value: String):
  case Asc extends OrderBy("ASC")
  case Desc extends OrderBy("DESC")
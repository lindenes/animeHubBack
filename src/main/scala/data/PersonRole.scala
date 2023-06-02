package data

enum PersonRole(val name: String):
  case Unknown extends PersonRole("Неизвестно")
  case Guest extends PersonRole("Гость")
  case SimpleUser extends PersonRole("Пользователь")
  case Moderator extends PersonRole("Модератор")
  case Admin extends PersonRole("Администратор")

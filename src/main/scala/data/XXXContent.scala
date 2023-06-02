package data

enum XXXContent(val description: String):

  case NotXXX extends XXXContent("Нет контента 18+")
  case YesXXX extends XXXContent("Есть контент 18+")

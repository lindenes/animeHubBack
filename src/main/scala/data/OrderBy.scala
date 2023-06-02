package data

enum OrderBy(val value: String):
  case Asc extends OrderBy("ASC")
  case Desc extends OrderBy("DESC")

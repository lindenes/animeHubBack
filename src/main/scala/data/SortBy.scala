package data

enum SortBy(val field: String):

  case Default extends SortBy("")
  case Year extends SortBy("year")
  case EpisodeCount extends SortBy("episode_count")
  case Title extends SortBy("title")
  case Rating extends SortBy("rating")

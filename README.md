# animeHubBack

Sorry I didn't add readmy right away.

This is the back of the AnimeHub website. It's made for a thesis for a university. This was my first time doing this project and I want to get other people's opinions and advice. 

Available registration, registration, add a post with information about anime, edit post, delete post, as well as the administrator and moderator functionality.

I used doobie , cats effect, http4s, scala version 3.3.0, and not much jdbc at all.


## TODO

- [ ] Wrap side effect-full operations into `cats.effect.IO`.
- [ ] Use Doobie everywhere.
- [ ] Maintain a single `doobie.Transactor` per app.
- [ ] Setup database migrations, e.g. using Flyway.
- [ ] Avoid passing raw JSONs around the codebase.
- [ ] Switch to newtypes with restricted constructors.
- [ ] Use Smithy4s/Tapir to generate clients/OpenAPI spec and reduce boilerplate (no more JSON, data models, newtypes).
- [ ] Pass configuration as environment variables.

## Localization
Would be nice to be able to conveniently switch the language through env var.

For example:
```scala
trait Language:
  def notAnAdmin: String
  def registered: String
  ...

object Language:
  private object English extends Language:
    override def notAnAdmin: String = "Not an admin"
    override def registered: String = "Registered"
    ...

  private object Russian extends Language:
    override def notAnAdmin: String = "Нужны права администратора"
    override def registered: String = "Успешно зарегистрирован"
    ...
```
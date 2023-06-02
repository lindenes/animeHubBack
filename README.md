# animeHubBack

Sorry I didn't add the README right away.

This is the backend of the AnimeHub website made as a thesis for a University. This was my first time doing this project and I want to get other people's opinions and advice. 

Available registration, logging in, adding a post with information about an anime title, editing and deleting a post, as well as the administrator and moderator functionality.

Libraries:
- [Doobie](https://tpolecat.github.io/doobie/)
- [Cats Effect](https://typelevel.org/cats-effect/)
- [http4s](https://http4s.org/)
- [Scala version 3.3.0](https://dotty.epfl.ch/)
- [A little bit of raw JDBC](https://docs.oracle.com/javase/tutorial/jdbc/basics/index.html)


## TODOs

- [ ] Wrap side effect-full operations into `cats.effect.IO`.
- [ ] Use Doobie everywhere for a DB access.
- [ ] Maintain a single `doobie.Transactor` per app.
- [ ] Setup database migrations, e.g. using [Flyway](https://flywaydb.org/).
- [ ] Avoid passing raw JSONs around the codebase.
- [ ] Switch to newtypes with restricted constructors, e.g. `opaque type`.
- [ ] Use [Smithy4s](https://github.com/disneystreaming/smithy4s) / [Tapir](https://tapir.softwaremill.com/en/latest/index.html) / [Endpoints](https://github.com/endpoints4s/endpoints4s) / etc. to generate clients/[OpenAPI](https://www.openapis.org/) spec and reduce the boilerplate (no more JSON, data models, newtypes).
- [ ] Pass configuration as environment variables.
- [ ] Packaging, e.g. [Docker](https://www.docker.com/) / [Docker Compose](https://docs.docker.com/compose/): [sbt-docker](https://github.com/marcuslonnberg/sbt-docker), [sbt-native-packager](https://github.com/sbt/sbt-native-packager).
- [ ] CI using GitHub Actions - you could use the newly defined `sbt validate`.
- [ ] Automatically issue, and perhaps merge / deploy (if passes the CI checks), PRs with dependencies updates using [Scala Steward](https://github.com/scala-steward-org/scala-steward).
- [ ] Deploy it somewhere.
- [ ] Probably: compile to a native, dynamic or static, executable, e.g. using [GraalVM](https://www.graalvm.org/latest/reference-manual/native-image/): [sbt-native-image](https://github.com/scalameta/sbt-native-image), [sbt-native-packager](https://github.com/sbt/sbt-native-packager) or [Scala Native](https://scala-native.org/en/stable/).
  - Warning: this may be painful.

## Localization
Also, would be nice to be able to conveniently switch the language through env var.

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
//package controller
//
//
//import cats.implicits._
//import org.http4s.circe._
//import org.http4s._
//import io.circe.generic.auto._
//import io.circe.syntax._
//
//import org.http4s.implicits._
//
//import org.http4s.server.blaze.BlazeServerBuilder
//
//
//import java.time.Year
//import java.util.UUID
//import scala.collection.mutable
//import scala.util.Try
//import cats.Monad
//import cats.effect.{Concurrent, ExitCode, IO, IOApp}
//import org.http4s.circe.jsonOf
//import org.http4s.dsl.Http4sDsl
//import org.http4s.dsl.impl.{OptionalValidatingQueryParamDecoderMatcher, QueryParamDecoderMatcher}
//import org.http4s.headers.`Content-Encoding`
//import org.http4s.server.Router
//
//
//object Http4sTutorial extends IOApp {
//
//  type Actor = String
//
//  case class Movie(id: String, title: String, year: Int, actors: List[String], director: String)
//
//  case class Director(firstName: String, lastName: String) {
//    override def toString: String = s"$firstName $lastName"
//  }
//
//  val snjl: Movie = Movie(
//    "6bcbca1e-efd3-411d-9f7c-14b872444fce",
//    "Zack Snyder's Justice League",
//    2021,
//    List("Henry Cavill", "Gaaaaal Godot", "Ezra Miller", "Ben Affleck", "Ray Fisher", "Jason Momoa"),
//    "Zack Snyder"
//  )
//
//  val movies: Map[String, Movie] = Map(snjl.id -> snjl)
//
//  object DirectorQueryParamMatcher extends QueryParamDecoderMatcher[String]("director")
//
//  implicit val yearQueryParamDecoder: QueryParamDecoder[Year] =
//    QueryParamDecoder[Int].emap { y =>
//      Try(Year.of(y))
//        .toEither
//        .leftMap { tr =>
//          ParseFailure(tr.getMessage, tr.getMessage)
//        }
//    }
//
//  object YearQueryParamMatcher extends OptionalValidatingQueryParamDecoderMatcher[Year]("year")
//
//  def movieRoutes[F[_] : Monad]: HttpRoutes[F] = {
//    val dsl = Http4sDsl[F]
//    import dsl._
//    HttpRoutes.of[F] {
//      case GET -> Root / "movies" :? DirectorQueryParamMatcher(director) +& YearQueryParamMatcher(maybeYear) =>
//        val movieByDirector = findMoviesByDirector(director)
//        maybeYear match {
//          case Some(y) =>
//            y.fold(
//              _ => BadRequest("The given year is not valid"),
//              { year =>
//                val moviesByDirAndYear =
//                  movieByDirector.filter(_.year == year.getValue)
//                Ok(moviesByDirAndYear.asJson)
//              }
//            )
//          case None => Ok(movieByDirector.asJson)
//        }
//      case GET -> Root / "movies" / UUIDVar(movieId) / "actors" =>
//        findMovieById(movieId).map(_.actors) match {
//          case Some(actors) => Ok(actors.asJson)
//          case _ => NotFound(s"No movie with id $movieId found")
//        }
//    }
//  }
//
//  private def findMovieById(movieId: UUID) =
//    movies.get(movieId.toString)
//
//  private def findMoviesByDirector(director: String): List[Movie] =
//    movies.values.filter(_.director == director).toList
//
//  object DirectorVar {
//    def unapply(str: String): Option[Director] = {
//      if (str.nonEmpty && str.matches(".* .*")) {
//        Try {
//          val splitStr = str.split(' ')
//          Director(splitStr(0), splitStr(1))
//        }.toOption
//      } else None
//    }
//  }
//
//  val directors: mutable.Map[Actor, Director] =
//    mutable.Map("Zack Snyder" -> Director("Zack", "Snyder"))
//
//  def directorRoutes[F[_] : Concurrent]: HttpRoutes[F] = {
//    val dsl = Http4sDsl[F]
//    import dsl._
//    implicit val directorDecoder: EntityDecoder[F, Director] = jsonOf[F, Director]
//    HttpRoutes.of[F] {
//      case GET -> Root / "directors" / DirectorVar(director) =>
//        directors.get(director.toString) match {
//          case Some(dir) => Ok(dir.asJson, Header.Raw(CIString("My-Custom-Header"), "value"))
//          case _ => NotFound(s"No director called $director found")
//        }
//      case req@POST -> Root / "directors" =>
//        for {
//          director <- req.as[Director]
//          _ = directors.put(director.toString, director)
//          res <- Ok.headers(`Content-Encoding`(ContentCoding.gzip))
//            .map(_.addCookie(ResponseCookie("My-Cookie", "value")))
//        } yield res
//    }
//  }
//
//  def allRoutes[F[_] : Concurrent]: HttpRoutes[F] = {
//    movieRoutes[F] <+> directorRoutes[F]
//  }
//
//  def allRoutesComplete[F[_] : Concurrent]: HttpApp[F] = {
//    allRoutes.orNotFound
//  }
//
//  import scala.concurrent.ExecutionContext.global
//
//  override def run(args: List[String]): IO[ExitCode] = {
//
//    val apis = Router(
//      "/api" -> Http4sTutorial.movieRoutes[IO],
//      "/api/private" -> Http4sTutorial.directorRoutes[IO]
//    ).orNotFound
//
//    BlazeServerBuilder[IO](global)
//      .bindHttp(8082, "localhost")
//      .withHttpApp(apis)
//      .resource
//      .use(_ => IO.never)
//      .as(ExitCode.Success)
//  }
//}

import slick.jdbc.MySQLProfile.api._

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}


object DataBaseConnection extends App {
  case class Player3(id: Long, name: String, country: String, dob: LocalDate)

  class PlayerTable(tag: Tag) extends Table[Player3](tag, None, "Player33")
  {
    override def * = (id, name, country, dob).mapTo[Player3]

    val id: Rep[Long] =               column[Long]("PlayerId", O.AutoInc, O.PrimaryKey)
    val name: Rep[String] =           column[String]("Name")
    val country: Rep[String] =        column[String]("Country")
    val dob: Rep[LocalDate] =         column[LocalDate]("Dob")
  }

  val db = Database.forConfig("mysql")
  println("start")

  val playerTable3: TableQuery[PlayerTable] = TableQuery[PlayerTable]
//  db.run(DBIO.seq(playerTable3.schema.create))

  val player2 = Player3(3L, "name3", "USA", LocalDate.now())
  val insertPlayerQuery = playerTable3 += player2
  val insertResult: Future[Int] = db.run(insertPlayerQuery)

//  val germanPlayersQuery = playerTable3.filter(_.country === "Germany")
//  val germanPlayers: Future[Seq[Player3]] = db.run[Seq[Player3]](germanPlayersQuery.result)
//
//  val test: Future[Player3] = germanPlayers.map(o => o.head)
//
//
//  test.onComplete {
//    case Success(value) => println(value)
//    case Failure(t) => t.printStackTrace()
//  }


  println("stop")
}

import plugins.C3P0ConnectionPoolFactory
import scala.util.Try
import scala.concurrent.Await
import scala.concurrent.duration._
import play.api.test.FakeRequest
import scalikejdbc._

/**
 *
 * @author ponkotuy
 * Date: 14/04/16.
 */
object UserShipDB {
  def main(args: Array[String]): Unit = {
    Class.forName("org.mariadb.jdbc.Driver")
    ConnectionPool.singleton("jdbc:mariadb://localhost:3306/myfleet", "myfleet", "myfleet")

    val count = Try { args(0).toInt }.getOrElse(20)
    val users = new UserIterator(0)
    users.take(count).foreach { user =>
      val result = controllers.UserView.ship(user).apply(FakeRequest())
      println(Await.result(result, 60.seconds))
    }
  }

  implicit val factory = C3P0ConnectionPoolFactory
}

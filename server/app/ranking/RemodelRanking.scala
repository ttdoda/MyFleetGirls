package ranking

import controllers.routes
import models.db.{Admiral, SlotItem}
import org.json4s.JValue
import ranking.common.{RankingData, RankingElement, Ranking}
import ranking.data.Count
import scalikejdbc._
import com.github.nscala_time.time.Imports._
import scala.collection.breakOut

/**
 * Date: 15/07/28.
  *
  * @author ponkotuy
 */
object RemodelRanking extends Ranking {
  import Ranking._
  import util.MFGDateUtil._

  override val id = 8
  override val title: String = "装備改修"
  override val comment: Seq[String] = List(comment7days, "装備改修は+の合計です")
  override val divClass: String = colmd3

  // Rankingを生成するのに使用
  override def rankingQuery(limit: Int): Seq[RankingElement] = {
    val ago7 = (DateTime.now(Tokyo) - 7.days).getMillis
    val si = SlotItem.si
    val remodelCounts: Map[Long, Int] = SlotItem.findAllBy(sqls.gt(si.created, ago7)).groupBy(_.memberId).mapValues { items =>
      items.map(_.level).sum
    }
    val admiralIds: Vector[Long] = remodelCounts.toVector.sortBy(-_._2).map(_._1).take(limit)
    val admiralNames: Map[Long, String] = Admiral.findAllIn(admiralIds).map(a => a.id -> a.nickname)(breakOut)
    admiralIds.flatMap { id =>
      for {
        name <- admiralNames.get(id)
        count <- remodelCounts.get(id)
      } yield {
        val url = routes.UserView.slotitem(id).toString
        RankingElement(id, name, Count(count), url, count)
      }
    }
  }

  override def decodeData(v: JValue): Option[RankingData] = Count.decode(v)
}

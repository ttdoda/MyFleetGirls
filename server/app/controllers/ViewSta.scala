package controllers

import javax.inject.Inject

import models.db
import models.join._
import models.query.{Period, SnapshotSearch}
import models.view.{CItem, CShip}
import org.json4s._
import org.json4s.native.Serialization.write
import play.api.mvc._
import ranking.common.{Ranking, RankingType}
import scalikejdbc._
import util.{MFGDateUtil, PeriodicalValue, Ymdh}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.Try

/**
 * Date: 14/06/11.
 */
class ViewSta @Inject()(implicit val ec: ExecutionContext) extends Controller {
  import ViewSta._
  import controllers.Common._

  def activities = actionAsync { Ok(views.html.sta.activities()) }

  def statistics(from: String, to: String) = actionAsync {
    val fromTo = Period.fromStr(from, to)
    val sCounts = db.CreateShip.materialCount(fromTo.where(sqls"cs.created")).take(50).takeWhile(_._2 > 1)
    val iCounts = db.CreateItem.materialCount(fromTo.whereOpt(sqls"ci.created")).take(50).takeWhile(_._2 > 1)
    Ok(views.html.sta.statistics(sCounts, iCounts, fromTo))
  }

  def cship(fuel: Int, ammo: Int, steel: Int, bauxite: Int, develop: Int, from: String, to: String) = actionAsync {
    val mat = Mat(fuel, ammo, steel, bauxite, develop)
    val fromTo = Period.fromStr(from, to)
    val cs = CShip(mat, fromTo)
    val counts = db.CreateShip.countByMatWithMaster(mat, fromTo.where(sqls"cs.created"))
    val graphJson = cshipGraphJson(counts, cs.title)
    val sum = counts.map(_._2).sum.toDouble
    val withRate = counts.map { case (ship, count) => (ship.name, count, count/sum) }
    val cships = db.CreateShip.findAllByMatWithName(mat, limit = 100)
    Ok(views.html.sta.cship(cs, graphJson, withRate, cships))
  }

  private def cshipGraphJson(counts: List[(db.MasterShipBase, Long)], title: String): String = {
    implicit val formats: Formats = DefaultFormats
    val sum = counts.map(_._2).sum.toDouble
    val sTypeName = db.MasterStype.findAll().map(ms => ms.id -> ms.name).toMap
    val sTypeCounts = counts.groupBy(it => sTypeName(it._1.stype)).mapValues(_.map(_._2).sum)
    val data = sTypeCounts.map { case (sname, sCount) =>
      val countByShip = counts.filter { case (ship, _) => sTypeName(ship.stype) == sname }
      val children = countByShip.map { case (ship, count) =>
        Map("name" -> s"${ship.name} $count(${toP(count/sum)}%)", "count" -> count)
      }
      Map("name" -> s"${sname} $sCount(${toP(sCount/sum)}%)", "children" -> children)
    }
    write(Map("name" -> title, "children" -> data))
  }

  def citem(fuel: Int, ammo: Int, steel: Int, bauxite: Int, sType: String, from: String, to: String) = actionAsync {
    val fromTo = Period.fromStr(from, to)
    val mat = ItemMat(fuel, ammo, steel, bauxite, sType)
    val citem = CItem(mat, fromTo)
    val ci = db.CreateItem.ci
    val mst = db.CreateItem.mst
    val citems = db.CreateItem.findAllByWithName(
      sqls.eq(ci.fuel, fuel).and.eq(ci.ammo, ammo).and.eq(ci.steel, steel).and.eq(ci.bauxite, bauxite).and.eq(mst.name, sType),
      limit = 100
    )
    val counts = db.CreateItem.countItemByMat(mat, fromTo.where(ci.created))
    val sum = counts.map(_._2).sum.toDouble
    val withRate = counts.map { case (item, count) => (item.name, count, count/sum) }
    val countJsonRaw = counts.map { case (item, count) =>
      val url = routes.ViewSta.fromShip().toString + s"#query=${item.name}"
      Map("label" -> item.name, "data" -> count, "url" -> url)
    }
    Ok(views.html.sta.citem(citem, write(countJsonRaw), withRate, citems))
  }

  def fromShip() = actionAsync { Ok(views.html.sta.from_ship()) }

  def dropStage() = actionAsync {
    val stages = countStageCache.apply()
    Ok(views.html.sta.drop_stage(stages))
  }

  def drop(area: Int, info: Int) = actionAsync {
    area match {
      case id if 21 until 42 contains id => Redirect(routes.ViewSta.drop1st(area, info))
      case _ =>
        val cells = db.BattleResult.dropedCells(area, info)
        Ok(views.html.sta.drop(Stage(area, info), cells))
    }
  }
  def drop1st(area: Int, info: Int) = actionAsync {
    area match {
      case id if (21 until 42 contains id) || id <= 6 =>
        val cells = db.BattleResult.dropedCells(area, info)
        Ok(views.html.sta.drop_1st(Stage(area, info), cells))
      case _ => Redirect(routes.ViewSta.drop(area, info))
    }
  }

  def dropAlpha(area: Int, info: Int) = actionAsync {
    area match {
      case id if 21 until 42 contains id => Redirect(routes.ViewSta.dropAlpha1st(area, info))
      case _ =>
        val cells = db.BattleResult.dropedCells(area, info)
        Ok(views.html.sta.drop_alpha(Stage(area, info), cells))
    }
  }
  def dropAlpha1st(area: Int, info: Int) = actionAsync {
    area match {
      case id if (21 until 42 contains id) || id <= 6 =>
        val cells = db.BattleResult.dropedCells(area, info)
        Ok(views.html.sta.drop_alpha_1st(Stage(area, info), cells))
      case _ => Redirect(routes.ViewSta.dropAlpha(area, info))
    }
  }

  def route(area: Int, info: Int) = actionAsync {
    area match {
      case id if 21 until 42 contains id => Redirect(routes.ViewSta.route1st(area, info))
      case _ => Ok(views.html.sta.route(Stage(area, info)))
    }
  }
  def route1st(area: Int, info: Int) = actionAsync {
    area match {
      case id if (21 until 42 contains id) || id <= 6 => Ok(views.html.sta.route_1st(Stage(area, info)))
      case _ => Redirect(routes.ViewSta.route(area, info))
    }
  }

  def routeFleet2nd(area: Int, info: Int, dep: Int, dest: Int, from: String, to: String) = actionAsync {
    routeFleet(area, info, dep, dest, from, to) { (stage, cDep, cDest, counts) =>
      views.html.sta.modal_route(stage, cDep, cDest, counts)
    }
  }

  def routeFleet1st(area: Int, info: Int, dep: Int, dest: Int, from: String, to: String) = actionAsync {
    routeFleet(area, info, dep, dest, from, to) { (stage, cDep, cDest, counts) =>
      views.html.sta.modal_route_1st(stage, cDep, cDest, counts)
    }
  }

  private def routeFleet(area: Int, info: Int, dep: Int, dest: Int, from: String, to: String)(view: (Stage, db.CellInfo, db.CellInfo, Seq[(Seq[String], Int)]) => play.twirl.api.Html): Result = {
    val period = Period.fromStr(from, to)
    val mr = db.MapRoute.mr
    val fleets = db.MapRoute.findFleetBy(
      sqls.eq(mr.areaId, area)
        .and.eq(mr.infoNo, info)
        .and.eq(mr.dep, dep)
        .and.eq(mr.dest, dest)
        .and.append(period.where(mr.created))
    )
    val counts = fleetCounts(fleets)
    val cDep = db.CellInfo.findOrDefault(area, info, dep)
    val cDest = db.CellInfo.findOrDefault(area, info, dest)
    Ok(view(Stage(area, info), cDep, cDest, counts))
  }

  private def fleetCounts(fleets: Seq[Seq[ShipWithName]]): Seq[(Seq[String], Int)] = {
    fleets.map { xs => xs.map(_.stype.name).sorted }
        .groupBy(identity).mapValues(_.size)
        .filterKeys(_.nonEmpty)
        .toList.sortBy(_._2).reverse.take(30)
  }

  def ranking() = actionAsync(Redirect(routes.ViewSta.rankingWithType("Admiral")))

  def rankingWithType(typ: String, yyyymmddhh: Int) = actionAsync {
    val ymdh = rankingYmdh(yyyymmddhh)
    RankingType.fromStr(typ).map { ranking =>
      Ok(views.html.sta.ranking(ranking, ymdh))
    }.getOrElse(NotFound("Not found page type"))
  }

  def rankingDetails(_ranking: String, yyyymmddhh: Int) = actionAsync {
    val ymdh = rankingYmdh(yyyymmddhh)
    Ranking.fromString(_ranking).map { ranking =>
      Ok(views.html.sta.modal_ranking(ranking, ymdh))
    }.getOrElse(NotFound("そのようなRankingは見つかりません"))
  }

  private def rankingYmdh(yyyymmddhh: Int)(implicit session: DBSession = AutoSession): Ymdh = {
    import MFGDateUtil._
    if(yyyymmddhh < 0) {
      db.MyfleetRanking.findNewestTime().getOrElse(Ymdh.now(Tokyo))
    } else Ymdh.fromInt(yyyymmddhh)
  }

  def shipList() = actionAsync {
    val ms = db.MasterShipBase.ms
    val ships = db.MasterShipBase.findAllWithStype(sqls.gt(ms.sortno, 0))
    Ok(views.html.sta.ship_list(ships, favCountTableByShip()))
  }

  def shipBook(sid: Int) = actionAsync {
    db.MasterShipBase.findAllInOneBy(sqls.eq(db.MasterShipBase.ms.id, sid)).headOption.map { master =>
      val ships = db.Ship.findByWithAdmiral(sid)
      val admiral1st = db.ShipImage.findAdmiral(sid)
      val admiral = db.ShipImage2nd.findAdmiralOfCard(sid).orElse(admiral1st)
      val admiralDmg = db.ShipImage2nd.findAdmiralOfCardDmg(sid).orElse(admiral1st)
      val yomes = db.YomeShip.findAllByWithAdmiral(sqls.eq(db.Ship.s.shipId, sid), 50)
      val admiralCount = db.Admiral.countAll()
      val heldRate = db.Ship.countAdmiral(sqls.eq(db.Ship.s.shipId, sid)).toDouble / admiralCount
      val bookCount = db.ShipBook.countBy(sqls.eq(db.ShipBook.sb.id, sid))
      val bookRate = if(bookCount >= 5) bookCount.toDouble / admiralCount else 0.0
      Ok(views.html.sta.ship_book(master, ships, admiral, admiralDmg, yomes, heldRate, bookRate))
    }.getOrElse(NotFound(s"Not Found ShipID: $sid"))
  }

  def remodelSlot() = actionAsync {
    val ids = db.RemodelSlot.findAllUniqueSlotId()
    val counts = db.Remodel.countAllFromBefore().withDefaultValue(0L)
    val slots = db.MasterSlotItem.findIn(ids)
    Ok(views.html.sta.remodel_slot(slots, counts))
  }

  def searchSnap(q: String, page: Int) = actionAsync {
    require(0 <= page && page < 10, "0 <= page < 10")
    val result = SnapshotSearch.search(q, page)
    Ok(views.html.sta.search_snap(result))
  }

  def honor() = actionAsync {
    val h = db.Honor.h
    val honors = db.Honor.findAllByWithAdmiral(sqls.eq(h.setBadge, true).and.eq(h.invisible, false))
    val withRates = HonorWithRate.fromWithAdmiral(honors).sortBy(-_.rate)
    Ok(views.html.sta.honor(withRates))
  }
}

object ViewSta {
  val StaBookURL = "/entire/sta/book/"

  def favCountTableByShip(): Map[Int, Long] = {
    val f = db.Favorite.f
    val favs = db.Favorite.countByURL(sqls.eq(f.first, "entire").and.eq(f.second, "sta").and.like(f.url, StaBookURL + "%"))
    favs.flatMap { case (url, _, count) =>
      Try { url.replace(StaBookURL, "").toInt }.map(_ -> count).toOption
    }.toMap.withDefaultValue(0L)
  }

  private def toP(d: Double): String = f"${d*100}%.1f"

  val countStageCache = new PeriodicalValue(1.hour, () => db.BattleResult.countAllByStage())

}

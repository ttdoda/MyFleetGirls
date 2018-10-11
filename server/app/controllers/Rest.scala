package controllers

import javax.inject.Inject

import com.github.nscala_time.time.Imports._
import com.ponkotuy.tool.Checksum
import models.db
import models.db.MapRank
import models.join.{ItemMat, MasterRemodelJson, ShipDrop, ShipWithFav}
import models.query.Period
import models.response.DropRate
import org.json4s.JsonDSL._
import org.json4s._
import play.api.mvc.{BaseController, ControllerComponents}
import scalikejdbc._

import scala.collection.{breakOut, mutable}
import scala.concurrent.ExecutionContext

/**
 *
 * @author ponkotuy
 * Date: 14/02/23
 */
class Rest @Inject()(val controllerComponents: ControllerComponents, implicit val ec: ExecutionContext) extends BaseController {
  import controllers.Common._

  def searchUser(q: String) = {
    if(q.isEmpty) {
      val b = db.Admiral.b
      returnJson(db.Admiral.findAllRandomWithLv(limit = 20))
    } else {
      returnJson(db.Admiral.findAllByLike(s"%$q%", limit = 20))
    }
  }

  def searchBaseUser(serverId: Int) = returnJson(db.Admiral.findAllByServer(serverId, limit = 20))

  def searchBaseUser(serverId: Int, name: String) = returnJson {
    val where = sqls"a.nickname like ${s"%$name%"}"
    db.Admiral.findAllByServer(serverId, where = where, limit = 20)
  }

  /** Createされた記録のあるMasterShipとMasterSlotItemを検索 */
  def searchMaster(q: String) = returnJson {
    val masterShips = db.MasterShipBase.findAllByLike(s"%$q%")
    val ships = masterShips.filter { ship => db.CreateShip.existsShip(ship.id) || db.BattleResult.existsShip(ship.id) }
    val msi = db.MasterSlotItem.msi
    val masterItems = db.MasterSlotItem.findAllBy(sqls.like(msi.name, s"%$q%"))
    val items = masterItems.filter { item => db.CreateItem.existsItem(item.id) }
    Map("ships" -> ships.distinct, "items" -> items)
  }

  /** Createされた記録のあるMasterShipを検索 */
  def searchMasterShip(q: String) = returnJson {
    val masterShips = db.MasterShipBase.findAllByLike(s"%$q%")
    masterShips.filter { ship => db.CreateShip.existsShip(ship.id) || db.BattleResult.existsShip(ship.id) }
  }

  def masterShipCount() = returnString { db.MasterShipBase.count() }
  def masterShipHash() = returnString {
    val names = db.MasterShipBase.findShipNames()
    Checksum.fromSeq(names)
  }

  def recipeFromShip(shipId: Int, from: String, to: String) = returnJson {
    val fromTo = Period.fromStr(from, to).where(sqls"cs.created")
    val allCounts = db.CreateShip.materialCount(fromTo).toMap
    val counts = db.CreateShip.materialCount(sqls"result_ship = ${shipId} and ${fromTo}")
    counts.map { case (mat, count) =>
      Map("mat" -> mat, "count" -> count, "sum" -> allCounts(mat))
    }
  }

  def dropFromShip(shipId: Int, from: String, to: String) = returnJson {
    val br = db.BattleResult.br
    val fromTo = Period.fromStr(from, to).where(br.created)
    val allCounts = db.BattleResult.countAllGroupByCells(fromTo).toMap
    val dropCounts = db.BattleResult.countAllGroupByCells(sqls.eq(br.getShipId, shipId).and.append(fromTo))
    dropCounts.map { case (cell, count) =>
      DropRate(cell, count, allCounts.getOrElse(cell, Long.MaxValue))
    }.sortBy(-_.count).map(_.toJson).take(51)
  }

  def recipeFromItem(itemId: Int, from: String, to: String) = returnJson {
    val fromTo = Period.fromStr(from, to).whereOpt(db.CreateItem.ci.created)
    val allCounts = {
      val counts = db.CreateItem.materialCount(fromTo)
      val builder = mutable.Map[ItemMat, Long]().withDefaultValue(0L)
      builder.sizeHint(counts.size)
      counts.foreach { case (mat, count) => builder(mat) += count }
      builder.toMap
    }
    val countWhere = sqls.toAndConditionOpt(fromTo, Some(sqls.eq(db.CreateItem.ci.slotitemId, itemId)))
    val counts = db.CreateItem.materialCount(countWhere)
    counts.map { case (mat, count) =>
      Map("mat" -> mat, "count" -> count, "sum" -> allCounts.lift(mat).getOrElse(Long.MaxValue))
    }
  }

  def dropCell(area: Int, info: Int, cell: Int, rank: String, from: String, to: String, mapRank: String) = returnJson {
    val fromTo = Period.fromStr(from ,to).where(sqls"br.created")
    val drops = db.BattleResult.countCellsGroupByDrop(area, info, cell, rank, fromTo.and(mapRankWhere(mapRank)))
    val sum = drops.map(_._2).sum.toDouble
    drops.map(dropToJson(sum))
  }

  def dropCellAlpha(area: Int, info: Int, alpha: String, rank: String, from: String, to: String, mapRank: String) = returnJson {
    val fromTo = Period.fromStr(from, to).where(sqls"br.created")
    val drops = db.BattleResult.countCellsAlphaGroupByDrop(area, info, alpha, rank, fromTo.and(mapRankWhere(mapRank)))
    val sum = drops.map(_._2).sum.toDouble
    drops.map(dropToJson(sum))
  }

  private def dropToJson(sum: Double)(sCount: (ShipDrop, Long)) = {
    val (drop, count) = sCount
    Extraction.decompose(drop).asInstanceOf[JObject] ~
      ("count" -> count) ~
      ("sum" -> sum) ~
      ("rate" -> f"${count / sum * 100}%.1f%%")
  }

  private def mapRankWhere(mapRank: String): Option[SQLSyntax] = {
    val br = db.BattleResult.br
    val mapRanks = MapRank.fromString(mapRank)
    if(mapRanks.isEmpty) None else Some(sqls.in(br.mapRank, mapRanks.map(_.v)(breakOut)))
  }

  def route(area: Int, info: Int, from: String, to: String) = returnJson {
    val period = Period.fromStr(from ,to)
    val routes = db.MapRoute.countCellsGroupByDest(area, info, period.where(sqls"mr.created"))
    routes.map { case (route, count) =>
      Extraction.decompose(route).asInstanceOf[JObject] ~ ("count" -> count)
    }
  }

  def cellInfo(area: Int, info: Int) = returnJson {
    val where = sqls"true"
      .append(if(area != -1) sqls" and area_id = ${area}" else sqls"")
      .append(if(info != -1) sqls" and info_no = ${info}" else sqls"")
    db.CellInfo.findAllBy(where)
  }

  def cellPosition(area: Int, info: Int) = returnJson {
    val cp = db.CellPosition.cp
    db.CellPosition.findAllBy(sqls.eq(cp.areaId, area).and.eq(cp.infoNo, info))
  }

  def cellPosition2nd(area: Int, info: Int, suffix: Int) = returnJson {
    val cp = db.CellPosition2nd.cp
    db.CellPosition2nd.findAllBy(sqls.eq(cp.areaId, area).and.eq(cp.infoNo, info).and.eq(cp.suffix, suffix))
  }

  def labelPosition(area: Int, info: Int, suffix: Int) = returnJson {
    val cp = db.LabelPosition.lp
    db.LabelPosition.findAllBy(sqls.eq(cp.areaId, area).and.eq(cp.infoNo, info).and.eq(cp.suffix, suffix))
  }

  def mapFrame(area: Int, info: Int, suffix: Int) = returnJson {
    val mf = db.MapFrame.mf
    db.MapFrame.findAllBy(sqls.eq(mf.areaId, area).and.eq(mf.infoNo, info).and.eq(mf.suffix, suffix))
  }

  def mapLayers(area: Int, info: Int) = returnJson {
    db.MapFrame.getLayers(area, info)
  }

  def mapBackground(area: Int, info: Int, suffix: Int) = returnJson {
    val mbn = db.MapBackgroundName.mbn
    db.MapBackgroundName.findAllBy(sqls.eq(mbn.areaId, area).and.eq(mbn.infoNo, info).and.eq(mbn.suffix, suffix))
  }

  def maps() = returnJson {
    db.MapRoute.findStageUnique()
  }

  def activities(from: Long, limit: Int, offset: Int) = returnJson {
    require(limit + offset <= 100, "limit + offset <= 100")
    readActivities(from, limit, offset).map(_.toJSON)
  }

  def bookShips(q: String) = returnJson {
    val ships = db.MasterShipBase.findAllWithStype(sqls"ms.sortno > 0 and ms.name like ${s"%$q%"}")
    val favCounts = ViewSta.favCountTableByShip()
    ships.groupBy(_.stypeName).toVector.sortBy(-_._2.length)
      .map { case (stype, xs) =>
        ("stype", stype) ~
          ("ships", xs.map(ShipWithFav.fromWithStype(_, favCounts).toJson))
      }
  }

  def remodelLogSummary(slotId: Int) = returnJson {
    import util.MFGDateUtil._
    val aWeekAgo = DateTime.now(Tokyo) - 1.week
    val r = db.RemodelSlot.r
    val where = sqls.eq(r.slotId, slotId).and.gt(r.created, aWeekAgo.getMillis)
    val logs = db.RemodelSlot.findAllWithSecondShipBy(where, 1000)
    val dates = logs.map(_.remodel.created).map(time => new DateTime(time, Tokyo))
    val dayOfWeekSummary = dates.map(_.getDayOfWeek).sorted.map(_.toString).distinct
    val secondShipCount = logs.map(_.ship).groupBy(_.map(_.name)).mapValues(_.size)
    val secondShipSummary = secondShipCount.toList.sortBy(-_._2).map(_._1)
    ("dayOfWeek" -> dayOfWeekSummary) ~
      ("secondShip" -> secondShipSummary)
  }

  def masterRemodel(slotId: Int) = returnJson {
    val origs = db.MasterRemodel.findAllByWithName(sqls"slotitem_id = ${slotId}")
    MasterRemodelJson.fromWithName(origs)
  }

  def missionHash() = returnString {
    val names = db.MasterMission.findAllName()
    Checksum.fromSeq(names)
  }

  def slotitemHash() = returnString {
    val names = db.MasterSlotItem.findAllName()
    Checksum.fromSeq(names)
  }

  def stypeHash() = returnString {
    val names = db.MasterStype.findAllName()
    Checksum.fromSeq(names)
  }
}

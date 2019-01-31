package controllers

import javax.inject.Inject

import com.ponkotuy.data._
import com.ponkotuy.data.master.{MasterRemodel, MapFrame, MapInfo => MapPositions}
import com.ponkotuy.value.KCServer
import models.db
import play.api.mvc._
import scalikejdbc.{AutoSession, DBSession}

import scala.concurrent.ExecutionContext

/**
 *
 * @author ponkotuy
 * Date: 14/02/21.
 */
class Post @Inject()(val controllerComponents: ControllerComponents, implicit val ec: ExecutionContext) extends BaseController {
  import controllers.Common._

  def basic = authAndParse[Basic] { case (auth, basic) =>
    val isChange = !db.Basic.findByUser(auth.id).exists(_.diff(basic) < 0.01)
    if(isChange) {
      db.Basic.create(basic, auth.id)
      Res.success
    } else {
      Res.noChange
    }
  }

  def admiralSettings = authAndParse[KCServer] { case (auth, server) =>
    db.UserSettings.setBase(auth.id, server.number)
    Res.success
  }

  def material = authAndParse[Material] { case (auth, material) =>
    val isChange = !db.Material.findByUser(auth.id).exists(_.diff(material) < 0.03)
    if(isChange) {
      db.Material.create(material, auth.id)
      Res.success
    } else {
      Res.noChange
    }
  }

  def ship2 = authAndParse[List[Ship]] { case (auth, ships) =>
    db.Ship.deleteAllByUser(auth.id)
    db.Ship.bulkInsert(ships, auth.id)
    db.ShipHistory.bulkInsert(ships, auth.id)
    Res.success
  }

  def updateShip() = authAndParse[List[Ship]] { case (auth, ships) =>
    db.Ship.bulkUpsert(ships, auth.id)
    db.ShipHistory.bulkInsert(ships, auth.id)
    Res.success
  }

  def ndock = authAndParse[List[NDock]] { case (auth, docks) =>
    db.NDock.deleteAllByUser(auth.id)
    docks.foreach(dock => db.NDock.create(dock, auth.id))
    Res.success
  }

  def createShip = authAndParse[CreateShipAndDock] { case (auth, CreateShipAndDock(ship, dock)) =>
    try {
      db.CreateShip.createFromKDock(ship, dock, auth.id)
    } catch {
      case _: Throwable =>
        Ok("Duplicate Entry")
    }
    Res.success
  }

  def createShip2 = authAndParse[CreateShipWithId] { case (auth, CreateShipWithId(ship, id)) =>
    db.CreateShip.create(ship, auth.id, id)
    Res.success
  }

  def createItem = authAndParse[CreateItem] { (auth, item) =>
    db.CreateItem.create(item, auth.id)
    for {
      id <- item.id
      slotitemId <- item.slotitemId
    } {
      db.SlotItem.create(auth.id, id, slotitemId)
    }
    Res.success
  }

  def kdock = authAndParse[List[KDock]] { case (auth, docks) =>
    db.KDock.deleteByUser(auth.id)
    db.KDock.bulkInsert(docks.filterNot(_.completeTime == 0), auth.id)
    Res.success
  }

  def deleteKDock() = authAndParse[DeleteKDock] { case (auth, kdock) =>
    db.KDock.destroy(auth.id, kdock.kDockId)
    Res.success
  }

  def deckPort = authAndParse[List[DeckPort]] { case (auth, decks) =>
    try {
      db.DeckPort.deleteByUser(auth.id)
      db.DeckPort.bulkInsertEntire(decks, auth.id)
    } catch {
      case e: Exception => e.printStackTrace()
    }
    Res.success
  }

  def shipBook = authAndParse[List[ShipBook]] { case (auth, ships) =>
    db.ShipBook.bulkUpsert(ships, auth.id)
    Res.success
  }

  def itemBook = authAndParse[List[ItemBook]] { case (auth, items) =>
    db.ItemBook.bulkUpsert(items, auth.id)
    Res.success
  }

  def mapInfo = authAndParse[List[MapInfo]] { case (auth, maps) =>
    if(maps.isEmpty) Res.noChange
    else {
      db.MapInfo.deleteAllByUser(auth.id)
      db.MapInfo.bulkInsert(maps, auth.id)
      Res.success
    }
  }

  def eventMapRank = authAndParse[EventMapRank] { case (auth, rank) =>
    if(db.MapInfo.updateRank(rank, auth.id)) Res.success else NotFound(s"Not found map_info.")
  }

  def slotItem = authAndParse[List[SlotItem]] { case (auth, items) =>
    db.SlotItem.deleteAllByUser(auth.id)
    db.SlotItem.bulkInsert(items, auth.id)
    Res.success
  }

  def battleResult = authAndParse[(BattleResult, MapStart)] { case (auth, (result, map)) =>
    db.AGOProgress.incWithBattle(auth.id, result, map)
    db.BattleResult.create(result, map, auth.id)
    Res.success
  }

  def mapStart = authAndParse[MapStart] { case (auth, _) =>
    db.AGOProgress.incSortie(auth.id)
    Res.success
  }

  def mapRoute = authAndParse[MapRoute] { case (auth, mapRoute) =>
    db.MapRoute.create(mapRoute, auth.id)
    Res.success
  }

  def questclearitem = authAndParse[QuestClearItem] { case (auth, request) =>
    db.QuestClearItem.bulkInsert(request, auth.id)
    Res.success
  }

  def questlist = authAndParse[List[Quest]] { case (auth, quests) =>
    db.Quest.bulkUpsert(quests, auth.id)
    Res.success
  }

  def remodelSlot() = authAndParse[RemodelSlotlist] { case (auth, request) =>
    db.RemodelSlot.bulkInsert(request, auth.id)
    Res.success
  }

  def remodel() = authAndParse[Remodel] { case (auth, request) =>
    db.Remodel.create(request, auth.id)
    for {
      afterSlot <- request.afterSlot
      item <- db.SlotItem.find(request.slotId, auth.id)
    } {
      db.SlotItem(
        auth.id, item.id, item.slotitemId, item.locked, afterSlot.level, item.alv, Some(System.currentTimeMillis())
      ).save()
    }
    Res.success
  }

  def masterRemodel() = authAndParse[MasterRemodel] { case (auth, request) =>
    db.MasterRemodel.createFromData(request, auth.id)
    Res.success
  }

  def mapFrame() = authAndParse[List[MapFrame]] { case (auth, request) =>
    if(db.MapFrame.find(request.head.areaId, request.head.infoNo, request.head.suffix, request.head.name, request.head.version).isDefined) Ok("Already exists")
    else {
      db.MapFrame.bulkInsert(request)
      Res.success
    }
  }

  def mapPositions() = authAndParse[MapPositions] { case (auth, request) =>
    val cellPositions = request.spots
    if(db.CellPosition2nd.find(cellPositions.head.areaId, cellPositions.head.infoNo, cellPositions.head.suffix, cellPositions.head.cell, cellPositions.head.version).isEmpty)
      db.CellPosition2nd.bulkInsert(cellPositions)

    val labelPositions = request.labels
    if(labelPositions.isEmpty) None
    else if(db.LabelPosition.find(labelPositions.head.areaId, labelPositions.head.infoNo, labelPositions.head.suffix, labelPositions.head.imageName, labelPositions.head.version).isEmpty)
      db.LabelPosition.bulkInsert(labelPositions)

    val bgNames = request.bg
    if(bgNames.isEmpty) None
    else if(db.MapBackgroundName.find(bgNames.head.areaId, bgNames.head.infoNo, bgNames.head.suffix, bgNames.head.imageName, bgNames.head.version).isEmpty)
      db.MapBackgroundName.bulkInsert(bgNames)

    Res.success
  }

  def ranking = Action { Ok("v1 is deprecated. use latest client.") }

  // rankingと変化は無いが、非対応clientがPOSTすると間違った値になるのでVerup
  def ranking2() = authAndParse[Ranking] { case (auth, request) =>
    db.Ranking.findNewest(auth.id) match {
      case None => insertRanking(auth, request)
      case Some(before) =>
        if(before.diff(request) > 0.0) insertRanking(auth, request) else Res.noChange
    }
  }

  private def insertRanking(auth: db.Admiral, rank: Ranking)(implicit session: DBSession = AutoSession) = {
    if(rank.nickname == auth.nickname) {
      db.Ranking.create(auth.id, rank.no, rank.rate, System.currentTimeMillis())
      Res.success
    } else {
      BadRequest("nickname mismatch")
    }
  }
}

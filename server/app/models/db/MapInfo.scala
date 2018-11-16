package models.db

import com.ponkotuy.data.{EventMapRank, Hp}
import models.join.Stage
import scalikejdbc._
import com.ponkotuy.data
import util.scalikejdbc.BulkInsert._

case class MapInfo(
    memberId: Long,
    id: Int,
    cleared: Boolean,
    defeatCount: Option[Int],
    requiredDefeatedCount: Option[Int],
    gaugeType: Option[Int],
    gaugeNum: Option[Int],
    airBaseDecks: Option[Int],
    nowHp: Option[Int],
    maxHp: Option[Int],
    state: Option[Int],
    rank: Option[MapRank],
    created: Long) {

  def destroy()(implicit session: DBSession = MapInfo.autoSession): Unit = MapInfo.destroy(this)(session)

  def abbr: String = stage.toString

  def areaId = id/10
  def infoNo = id%10

  lazy val stage: Stage = Stage(areaId, infoNo)

}


object MapInfo extends SQLSyntaxSupport[MapInfo] {

  override val tableName = "map_info"

  override val columns = Seq("member_id", "id", "cleared", "defeat_count", "required_defeated_count", "gauge_type", "gauge_num", "air_base_decks", "now_hp", "max_hp", "state", "rank", "created")

  def apply(mi: ResultName[MapInfo])(rs: WrappedResultSet): MapInfo = new MapInfo(
    memberId = rs.get(mi.memberId),
    id = rs.get(mi.id),
    cleared = rs.get(mi.cleared),
    defeatCount = rs.get(mi.defeatCount),
    requiredDefeatedCount = rs.get(mi.requiredDefeatedCount),
    gaugeType = rs.get(mi.gaugeType),
    gaugeNum = rs.get(mi.gaugeNum),
    airBaseDecks = rs.get(mi.airBaseDecks),
    nowHp = rs.get(mi.nowHp),
    maxHp = rs.get(mi.maxHp),
    state = rs.get(mi.state),
    rank = rs.get[Option[Int]](mi.rank).flatMap(MapRank.fromInt),
    created = rs.get(mi.created)
  )

  lazy val mi = MapInfo.syntax("mi")

  override val autoSession = AutoSession

  def find(id: Int, memberId: Long)(implicit session: DBSession = autoSession): Option[MapInfo] = {
    withSQL {
      select.from(MapInfo as mi).where.eq(mi.id, id).and.eq(mi.memberId, memberId)
    }.map(MapInfo(mi.resultName)).single().apply()
  }

  def findStage(stage: Stage, memberId: Long)(implicit session: DBSession = autoSession): Option[MapInfo] =
    find(stage.area * 10 + stage.info, memberId)

  def findAll()(implicit session: DBSession = autoSession): List[MapInfo] = {
    withSQL(select.from(MapInfo as mi)).map(MapInfo(mi.resultName)).list().apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls"count(1)").from(MapInfo as mi)).map(rs => rs.long(1)).single().apply().get
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[MapInfo] = {
    withSQL {
      select.from(MapInfo as mi).where.append(sqls"${where}")
    }.map(MapInfo(mi.resultName)).list().apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls"count(1)").from(MapInfo as mi).where.append(sqls"${where}")
    }.map(_.long(1)).single().apply().get
  }

  def create(
      memberId: Long,
      id: Int,
      cleared: Boolean,
      defeatCount: Option[Int],
      requiredDefeatedCount: Option[Int],
      gaugeType: Option[Int],
      gaugeNum: Option[Int],
      airBaseDecks: Option[Int],
      nowHp: Option[Int],
      maxHp: Option[Int],
      state: Option[Int],
      rank: Option[MapRank],
      created: Long)(implicit session: DBSession = autoSession): Unit = {
    withSQL {
      insert.into(MapInfo).columns(
        column.memberId,
        column.id,
        column.cleared,
        column.defeatCount,
        column.requiredDefeatedCount,
        column.gaugeType,
        column.gaugeNum,
        column.airBaseDecks,
        column.nowHp,
        column.maxHp,
        column.state,
        column.rank,
        column.created
      ).values(
          memberId,
          id,
          cleared,
          defeatCount,
          requiredDefeatedCount,
          gaugeType,
          gaugeNum,
          airBaseDecks,
          nowHp,
          maxHp,
          state,
          rank.map(_.v),
          created
        )
    }.update().apply()
  }

  def bulkInsert(xs: Seq[data.MapInfo], memberId: Long)(implicit session: DBSession = autoSession): Unit = {
    assert(xs.nonEmpty)
    val es = xs.map(_.eventMap)
    val hps: Seq[Option[Hp]] = es.map(_.flatMap(_.hp))
    val now = System.currentTimeMillis()
    applyUpdate {
      insert.into(MapInfo)
        .columns(
            column.memberId,
            column.id,
            column.cleared,
            column.defeatCount,
            column.requiredDefeatedCount,
            column.gaugeType,
            column.gaugeNum,
            column.airBaseDecks,
            column.nowHp,
            column.maxHp,
            column.state,
            column.rank,
            column.created)
        .multiValues(
            Seq.fill(xs.size)(memberId),
            xs.map(_.id),
            xs.map(_.cleared),
            xs.map(_.defeatedCount),
            xs.map(_.requiredDefeatedCount),
            xs.map(_.gaugeType),
            xs.map(_.gaugeNum),
            xs.map(_.airBaseDecks),
            hps.map(_.map(_.now)),
            hps.map(_.map(_.max)),
            es.map(_.map(_.state)),
            es.map(_.flatMap(_.rank)),
            Seq.fill(xs.size)(now))
    }
  }

  def updateRank(rank: EventMapRank, memberId: Long)(implicit session: DBSession = autoSession): Boolean = {
    applyUpdate {
      update(MapInfo)
          .set(column.rank -> rank.rank)
          .where.eq(column.memberId, memberId).and.eq(column.id, rank.mapAreaId * 10 + rank.mapNo)
    } > 0
  }

  def destroy(entity: MapInfo)(implicit session: DBSession = autoSession): Unit = {
    withSQL {
      delete.from(MapInfo).where.eq(column.id, entity.id).and.eq(column.memberId, entity.memberId)
    }.update().apply()
  }

  def deleteAllByUser(memberId: Long)(implicit session: DBSession = autoSession): Unit = applyUpdate {
    delete.from(MapInfo).where.eq(MapInfo.column.memberId, memberId)
  }

}

sealed abstract class MapRank(val v: Int, val str: String)

object MapRank {
  object NoChoice extends MapRank(0, "")
  object Hei extends MapRank(1, "hei")
  object Otsu extends MapRank(2, "otsu")
  object Ko extends MapRank(3, "ko")

  val values = Vector(NoChoice, Hei, Otsu, Ko)
  val enables = Vector(Hei, Otsu, Ko)

  def fromInt(v: Int): Option[MapRank] = values.find(_.v == v)
  def fromString(str: String): Set[MapRank] =
    enables.filter { rank => str.contains(rank.str) }.toSet
}

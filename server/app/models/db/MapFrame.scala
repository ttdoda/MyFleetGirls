package models.db

import com.ponkotuy.data.master

import scalikejdbc._
import util.scalikejdbc.BulkInsert._

case class MapFrame(
  areaId: Int,
  infoNo: Int,
  suffix: Int,
  name: String,
  posX: Int,
  posY: Int,
  width: Int,
  height: Int,
  version: Int) {

  def save()(implicit session: DBSession = MapFrame.autoSession): MapFrame = MapFrame.save(this)(session)

  def destroy()(implicit session: DBSession = MapFrame.autoSession): Unit = MapFrame.destroy(this)(session)

}

object MapFrame extends SQLSyntaxSupport[MapFrame] {

  override val tableName = "map_frame"

  override val columns = Seq("area_id", "info_no", "suffix", "name", "pos_x", "pos_y", "width", "height", "version")

  def apply(mf: SyntaxProvider[MapFrame])(rs: WrappedResultSet): MapFrame = autoConstruct(rs, mf)
  def apply(mf: ResultName[MapFrame])(rs: WrappedResultSet): MapFrame = autoConstruct(rs, mf)

  val mf = MapFrame.syntax("mf")

  override val autoSession = AutoSession

  def find(areaId: Int, infoNo: Int, suffix: Int, name: String, version: Int)(implicit session: DBSession = autoSession): Option[MapFrame] = {
    withSQL {
      select.from(MapFrame as mf)
          .where.eq(mf.areaId, areaId).and.eq(mf.infoNo, infoNo).and.eq(mf.suffix, suffix).and.eq(mf.name, name).and.eq(mf.version, version)
    }.map(MapFrame(mf.resultName)).single().apply()
  }

  def getLayers(areaId: Int, infoNo: Int)(implicit session: DBSession = autoSession): List[Int] = {
    withSQL {
      select(sqls.distinct(mf.suffix)).from(MapFrame as mf)
          .where.eq(mf.areaId, areaId).and.eq(mf.infoNo, infoNo).and.gt(mf.suffix, 0)
          .orderBy(mf.suffix.asc)
    }.map(_.int(1)).list().apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[MapFrame] = {
    withSQL(select.from(MapFrame as mf)).map(MapFrame(mf.resultName)).list().apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(MapFrame as mf)).map(rs => rs.long(1)).single().apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[MapFrame] = {
    withSQL {
      select.from(MapFrame as mf).where.append(where)
    }.map(MapFrame(mf.resultName)).single().apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[MapFrame] = {
    withSQL {
      select.from(MapFrame as mf).where.append(where)
    }.map(MapFrame(mf.resultName)).list().apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(MapFrame as mf).where.append(where)
    }.map(_.long(1)).single().apply().get
  }

  def create(mf: master.MapFrame)(implicit session: DBSession = autoSession): MapFrame = {
    withSQL {
      insert.into(MapFrame).namedValues(
        column.areaId -> mf.areaId, column.infoNo -> mf.infoNo,
        column.suffix -> mf.suffix, column.name -> mf.name,
        column.posX -> mf.posX, column.posY -> mf.posY,
        column.width -> mf.width, column.height -> mf.height,
        column.version -> mf.version
      )
    }.update().apply()
    MapFrame(
      mf.areaId,
      mf.infoNo,
      mf.suffix,
      mf.name,
      mf.posX,
      mf.posY,
      mf.width,
      mf.height,
      mf.version)
  }

  def bulkInsert(mf: Seq[master.MapFrame])(implicit session: DBSession = autoSession): Unit = {
    applyUpdate {
      insert.into(MapFrame).columns(
        column.areaId, column.infoNo,
        column.suffix, column.name,
        column.posX, column.posY,
        column.width, column.height,
        column.version
      ).multiValues(
          mf.map(_.areaId), mf.map(_.infoNo),
          mf.map(_.suffix), mf.map(_.name),
          mf.map(_.posX), mf.map(_.posY),
          mf.map(_.width), mf.map(_.height),
          mf.map(_.version)
        )
    }
  }

  def save(entity: MapFrame)(implicit session: DBSession = autoSession): MapFrame = {
    withSQL {
      update(MapFrame).set(
        column.areaId -> entity.areaId,
        column.infoNo -> entity.infoNo,
        column.suffix -> entity.suffix,
        column.name -> entity.name,
        column.posX -> entity.posX,
        column.posY -> entity.posY,
        column.width -> entity.width,
        column.height -> entity.height
      ).where.eq(column.areaId, entity.areaId).and.eq(column.infoNo, entity.infoNo).and.eq(column.suffix, entity.suffix).and.eq(column.name, entity.name).and.eq(column.version, entity.version)

    }.update().apply()
    entity
  }

  def destroy(entity: MapFrame)(implicit session: DBSession = autoSession): Unit = {
    withSQL {
      delete.from(MapFrame).where.eq(column.areaId, entity.areaId).and.eq(column.infoNo, entity.infoNo).and.eq(column.suffix, entity.suffix).and.eq(column.name, entity.name).and.eq(column.version, entity.version)
    }.update().apply()
  }

}

package models.db

import com.ponkotuy.data.master

import scalikejdbc._
import util.scalikejdbc.BulkInsert._

case class MapData(
  areaId: Int,
  infoNo: Int,
  suffix: Int,
  name: String,
  frameX: Int,
  frameY: Int,
  frameW: Int,
  frameH: Int,
  version: Int) {

  def save()(implicit session: DBSession = MapData.autoSession): MapData = MapData.save(this)(session)

  def destroy()(implicit session: DBSession = MapData.autoSession): Unit = MapData.destroy(this)(session)

}


object MapData extends SQLSyntaxSupport[MapData] {

  override val tableName = "map_data"

  override val columns = Seq("area_id", "info_no", "suffix", "name", "frame_x", "frame_y", "frame_w", "frame_h", "version")

  def apply(md: SyntaxProvider[MapData])(rs: WrappedResultSet): MapData = autoConstruct(rs, md)
  def apply(md: ResultName[MapData])(rs: WrappedResultSet): MapData = autoConstruct(rs, md)

  val md = MapData.syntax("md")

  override val autoSession = AutoSession

  def find(areaId: Int, infoNo: Int, suffix: Int, name: String, version: Int = 0)(implicit session: DBSession = autoSession): Option[MapData] = {
    if(version != 0) findWithVersion(areaId, infoNo, suffix, name, version)
    else {
      withSQL {
        select.from(MapData as md)
            .where.eq(md.areaId, areaId).and.eq(md.infoNo, infoNo).and.eq(md.suffix, suffix).and.eq(md.name, name)
            .orderBy(md.version.desc).limit(1)
      }.map(MapData(md.resultName)).single().apply()
    }
  }

  private def findWithVersion(areaId: Int, infoNo: Int, suffix: Int, name: String, version: Int = 0)(implicit session: DBSession = autoSession): Option[MapData] = {
    withSQL {
      select.from(MapData as md)
          .where.eq(md.areaId, areaId).and.eq(md.infoNo, infoNo).and.eq(md.suffix, suffix).and.eq(md.name, name).and.eq(md.version, version)
    }.map(MapData(md.resultName)).single().apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[MapData] = {
    withSQL(select.from(MapData as md)).map(MapData(md.resultName)).list().apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(MapData as md)).map(rs => rs.long(1)).single().apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[MapData] = {
    withSQL {
      select.from(MapData as md).where.append(where)
    }.map(MapData(md.resultName)).single().apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[MapData] = {
    withSQL {
      select.from(MapData as md).where.append(where)
    }.map(MapData(md.resultName)).list().apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(MapData as md).where.append(where)
    }.map(_.long(1)).single().apply().get
  }

  def create(mf: master.MapFrame)(implicit session: DBSession = autoSession): MapData = {
    withSQL {
      insert.into(MapData).namedValues(
        column.areaId -> mf.areaId, column.infoNo -> mf.infoNo,
        column.suffix -> mf.suffix, column.name -> mf.name,
        column.frameX -> mf.frameX, column.frameY -> mf.frameY,
        column.frameW -> mf.frameW, column.frameH -> mf.frameH,
        column.version -> mf.version
      )
    }.update().apply()
    MapData(
      mf.areaId,
      mf.infoNo,
      mf.suffix,
      mf.name,
      mf.frameX,
      mf.frameY,
      mf.frameW,
      mf.frameH,
      mf.version)
  }

  def bulkInsert(mf: Seq[master.MapFrame])(implicit session: DBSession = autoSession): Unit = {
    applyUpdate {
      insert.into(MapData).columns(
        column.areaId, column.infoNo,
        column.suffix, column.name,
        column.frameX, column.frameY,
        column.frameW, column.frameH,
        column.version
      ).multiValues(
          mf.map(_.areaId), mf.map(_.infoNo),
          mf.map(_.suffix), mf.map(_.name),
          mf.map(_.frameX), mf.map(_.frameY),
          mf.map(_.frameW), mf.map(_.frameH),
          mf.map(_.version)
        )
    }
  }

  def save(entity: MapData)(implicit session: DBSession = autoSession): MapData = {
    withSQL {
      update(MapData).set(
        column.areaId -> entity.areaId,
        column.infoNo -> entity.infoNo,
        column.suffix -> entity.suffix,
        column.name -> entity.name,
        column.frameX -> entity.frameX,
        column.frameY -> entity.frameY,
        column.frameW -> entity.frameW,
        column.frameH -> entity.frameH
      ).where.eq(column.areaId, entity.areaId).and.eq(column.infoNo, entity.infoNo).and.eq(column.suffix, entity.suffix).and.eq(column.name, entity.name).and.eq(column.version, entity.version)

    }.update().apply()
    entity
  }

  def destroy(entity: MapData)(implicit session: DBSession = autoSession): Unit = {
    withSQL {
      delete.from(MapData).where.eq(column.areaId, entity.areaId).and.eq(column.infoNo, entity.infoNo).and.eq(column.suffix, entity.suffix).and.eq(column.name, entity.name).and.eq(column.version, entity.version)
    }.update().apply()
  }

}

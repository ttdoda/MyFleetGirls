package models.db

import com.ponkotuy.data.master

import scalikejdbc._
import util.scalikejdbc.BulkInsert._

case class MapData(
  areaId: Int,
  infoNo: Int,
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

  override val columns = Seq("area_id", "info_no", "name", "frame_x", "frame_y", "frame_w", "frame_h", "version")

  def apply(md: SyntaxProvider[MapData])(rs: WrappedResultSet): MapData = autoConstruct(rs, md)
  def apply(md: ResultName[MapData])(rs: WrappedResultSet): MapData = autoConstruct(rs, md)

  val md = MapData.syntax("md")

  override val autoSession = AutoSession

  def find(areaId: Int, infoNo: Int, name: String, version: Int = 0)(implicit session: DBSession = autoSession): Option[MapData] = {
    if(version != 0) findWithVersion(areaId, infoNo, name, version)
    else {
      withSQL {
        select.from(MapData as md)
            .where.eq(md.areaId, areaId).and.eq(md.name, name).and.eq(md.infoNo, infoNo)
            .orderBy(md.version.desc).limit(1)
      }.map(MapData(md.resultName)).single().apply()
    }
  }

  private def findWithVersion(areaId: Int, infoNo: Int, name: String, version: Int = 0)(implicit session: DBSession = autoSession): Option[MapData] = {
    withSQL {
      select.from(MapData as md)
          .where.eq(md.areaId, areaId).and.eq(md.name, name).and.eq(md.infoNo, infoNo).and.eq(md.version, version)
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

  def create(md: master.MapData)(implicit session: DBSession = autoSession): MapData = {
    withSQL {
      insert.into(MapData).namedValues(
        column.areaId -> md.areaId, column.infoNo -> md.infoNo, column.name -> md.name,
        column.frameX -> md.frameX, column.frameY -> md.frameY,
        column.frameW -> md.frameW, column.frameH -> md.frameH,
        column.version -> md.version
      )
    }.update().apply()
    MapData(
      md.areaId,
      md.infoNo,
      md.name,
      md.frameX,
      md.frameY,
      md.frameW,
      md.frameH,
      md.version)
  }

  def bulkInsert(md: Seq[master.MapData])(implicit session: DBSession = autoSession): Unit = {
    applyUpdate {
      insert.into(MapData).columns(
        column.areaId, column.infoNo, column.name,
        column.frameX, column.frameY,
        column.frameW, column.frameH,
        column.version
      ).multiValues(
          md.map(_.areaId), md.map(_.infoNo), md.map(_.name),
          md.map(_.frameX), md.map(_.frameY),
          md.map(_.frameW), md.map(_.frameH),
          md.map(_.version)
        )
    }
  }

  def save(entity: MapData)(implicit session: DBSession = autoSession): MapData = {
    withSQL {
      update(MapData).set(
        column.areaId -> entity.areaId,
        column.infoNo -> entity.infoNo,
        column.name -> entity.name,
        column.frameX -> entity.frameX,
        column.frameY -> entity.frameY,
        column.frameW -> entity.frameW,
        column.frameH -> entity.frameH
      ).where.eq(column.areaId, entity.areaId).and.eq(column.name, entity.name).and.eq(column.infoNo, entity.infoNo).and.eq(column.version, entity.version)

    }.update().apply()
    entity
  }

  def destroy(entity: MapData)(implicit session: DBSession = autoSession): Unit = {
    withSQL {
      delete.from(MapData).where.eq(column.areaId, entity.areaId).and.eq(column.name, entity.name).and.eq(column.infoNo, entity.infoNo).and.eq(column.version, entity.version)
    }.update().apply()
  }

}

package models.db

import com.ponkotuy.data.master

import scalikejdbc._
import util.scalikejdbc.BulkInsert._

case class MapBackgroundName(
  areaId: Int,
  infoNo: Int,
  suffix: Int,
  priority: Int,
  imageName: String,
  version: Int) {

  def save()(implicit session: DBSession = MapBackgroundName.autoSession): MapBackgroundName = MapBackgroundName.save(this)(session)

  def destroy()(implicit session: DBSession = MapBackgroundName.autoSession): Unit = MapBackgroundName.destroy(this)(session)

}


object MapBackgroundName extends SQLSyntaxSupport[MapBackgroundName] {

  override val tableName = "map_background_name"

  override val columns = Seq("area_id", "info_no", "suffix", "priority", "image_name", "version")

  def apply(mbn: SyntaxProvider[MapBackgroundName])(rs: WrappedResultSet): MapBackgroundName = autoConstruct(rs, mbn)
  def apply(mbn: ResultName[MapBackgroundName])(rs: WrappedResultSet): MapBackgroundName = autoConstruct(rs, mbn)

  val mbn = MapBackgroundName.syntax("mbn")

  override val autoSession = AutoSession

  def find(areaId: Int, infoNo: Int, suffix: Int, imageName: String, version: Int)(implicit session: DBSession = autoSession): Option[MapBackgroundName] = {
    withSQL {
      select.from(MapBackgroundName as mbn).where.eq(mbn.areaId, areaId).and.eq(mbn.infoNo, infoNo).and.eq(mbn.suffix, suffix).and.eq(mbn.imageName, imageName).and.eq(mbn.version, version)
    }.map(MapBackgroundName(mbn.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[MapBackgroundName] = {
    withSQL(select.from(MapBackgroundName as mbn)).map(MapBackgroundName(mbn.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(MapBackgroundName as mbn)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[MapBackgroundName] = {
    withSQL {
      select.from(MapBackgroundName as mbn).where.append(where)
    }.map(MapBackgroundName(mbn.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[MapBackgroundName] = {
    withSQL {
      select.from(MapBackgroundName as mbn).where.append(where)
          .orderBy(mbn.priority.asc)
    }.map(MapBackgroundName(mbn.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(MapBackgroundName as mbn).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(bn: master.BackgroundName)(implicit session: DBSession = autoSession): MapBackgroundName = {
    withSQL {
      insert.into(MapBackgroundName).namedValues(
        column.areaId -> bn.areaId,
        column.infoNo -> bn.infoNo,
        column.suffix -> bn.suffix,
        column.priority -> bn.priority,
        column.imageName -> bn.imageName,
        column.version -> bn.version
      )
    }.update().apply()
    MapBackgroundName(
      bn.areaId,
      bn.infoNo,
      bn.suffix,
      bn.priority,
      bn.imageName,
      bn.version)
  }

  def bulkInsert(bn: Seq[master.BackgroundName])(implicit session: DBSession = autoSession): Unit = {
    applyUpdate {
      insert.into(MapBackgroundName).columns(
        column.areaId,
        column.infoNo,
        column.suffix,
        column.priority,
        column.imageName,
        column.version
      ).multiValues(
          bn.map(_.areaId),
          bn.map(_.infoNo),
          bn.map(_.suffix),
          bn.map(_.priority),
          bn.map(_.imageName),
          bn.map(_.version)
        )
    }
  }

  def save(entity: MapBackgroundName)(implicit session: DBSession = autoSession): MapBackgroundName = {
    withSQL {
      update(MapBackgroundName).set(
        column.areaId -> entity.areaId,
        column.infoNo -> entity.infoNo,
        column.suffix -> entity.suffix,
        column.priority -> entity.priority,
        column.imageName -> entity.imageName
      ).where.eq(column.areaId, entity.areaId).and.eq(column.imageName, entity.imageName).and.eq(column.infoNo, entity.infoNo).and.eq(column.suffix, entity.suffix).and.eq(column.version, entity.version)
    }.update.apply()
    entity
  }

  def destroy(entity: MapBackgroundName)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(MapBackgroundName).where.eq(column.areaId, entity.areaId).and.eq(column.imageName, entity.imageName).and.eq(column.infoNo, entity.infoNo).and.eq(column.suffix, entity.suffix).and.eq(column.version, entity.version) }.update.apply()
  }

}

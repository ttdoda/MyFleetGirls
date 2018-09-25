package models.db

import com.ponkotuy.data.master

import scalikejdbc._
import util.scalikejdbc.BulkInsert._

case class LabelPosition(
  areaId: Int,
  infoNo: Int,
  suffix: Int,
  imageName: String,
  posX: Int,
  posY: Int,
  version: Int) {

  def save()(implicit session: DBSession = LabelPosition.autoSession): LabelPosition = LabelPosition.save(this)(session)

  def destroy()(implicit session: DBSession = LabelPosition.autoSession): Unit = LabelPosition.destroy(this)(session)

}

object LabelPosition extends SQLSyntaxSupport[LabelPosition] {

  override val tableName = "label_position"

  override val columns = Seq("area_id", "info_no", "suffix", "image_name", "pos_x", "pos_y", "version")

  def apply(lp: SyntaxProvider[LabelPosition])(rs: WrappedResultSet): LabelPosition = autoConstruct(rs, lp)
  def apply(lp: ResultName[LabelPosition])(rs: WrappedResultSet): LabelPosition = autoConstruct(rs, lp)

  val lp = LabelPosition.syntax("lp")

  override val autoSession = AutoSession

  def find(areaId: Int, infoNo: Int, suffix: Int, imageName: String, version: Int)(implicit session: DBSession = autoSession): Option[LabelPosition] = {
    withSQL {
      select.from(LabelPosition as lp)
          .where.eq(lp.areaId, areaId).and.eq(lp.infoNo, infoNo).and.eq(lp.suffix, suffix).and.eq(lp.imageName, imageName).and.eq(lp.version, version)
    }.map(LabelPosition(lp.resultName)).single().apply()
  }

  def getLayers(areaId: Int, infoNo: Int)(implicit session: DBSession = autoSession): List[Int] = {
    withSQL {
      select(sqls.distinct(lp.suffix)).from(LabelPosition as lp)
          .where.eq(lp.areaId, areaId).and.eq(lp.infoNo, infoNo).and.gt(lp.suffix, 0)
          .orderBy(lp.suffix.asc)
    }.map(_.int(1)).list().apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[LabelPosition] = {
    withSQL(select.from(LabelPosition as lp)).map(LabelPosition(lp.resultName)).list().apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(LabelPosition as lp)).map(rs => rs.long(1)).single().apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[LabelPosition] = {
    withSQL {
      select.from(LabelPosition as lp).where.append(where)
    }.map(LabelPosition(lp.resultName)).single().apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[LabelPosition] = {
    withSQL {
      select.from(LabelPosition as lp).where.append(where)
    }.map(LabelPosition(lp.resultName)).list().apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(LabelPosition as lp).where.append(where)
    }.map(_.long(1)).single().apply().get
  }

  def create(lp: master.LabelPosition)(implicit session: DBSession = autoSession): LabelPosition = {
    withSQL {
      insert.into(LabelPosition).namedValues(
        column.areaId -> lp.areaId, column.infoNo -> lp.infoNo,
        column.suffix -> lp.suffix, column.imageName -> lp.imageName,
        column.posX -> lp.posX, column.posY -> lp.posY,
        column.version -> lp.version
      )
    }.update().apply()
    LabelPosition(
      lp.areaId,
      lp.infoNo,
      lp.suffix,
      lp.imageName,
      lp.posX,
      lp.posY,
      lp.version)
  }

  def bulkInsert(lp: Seq[master.LabelPosition])(implicit session: DBSession = autoSession): Unit = {
    applyUpdate {
      insert.into(LabelPosition).columns(
        column.areaId, column.infoNo,
        column.suffix, column.imageName,
        column.posX, column.posY,
        column.version
      ).multiValues(
          lp.map(_.areaId), lp.map(_.infoNo),
          lp.map(_.suffix), lp.map(_.imageName),
          lp.map(_.posX), lp.map(_.posY),
          lp.map(_.version)
        )
    }
  }

  def save(entity: LabelPosition)(implicit session: DBSession = autoSession): LabelPosition = {
    withSQL {
      update(LabelPosition).set(
        column.areaId -> entity.areaId,
        column.infoNo -> entity.infoNo,
        column.suffix -> entity.suffix,
        column.imageName -> entity.imageName,
        column.posX -> entity.posX,
        column.posY -> entity.posY
      ).where.eq(column.areaId, entity.areaId).and.eq(column.infoNo, entity.infoNo).and.eq(column.suffix, entity.suffix).and.eq(column.imageName, entity.imageName).and.eq(column.version, entity.version)

    }.update().apply()
    entity
  }

  def destroy(entity: LabelPosition)(implicit session: DBSession = autoSession): Unit = {
    withSQL {
      delete.from(LabelPosition).where.eq(column.areaId, entity.areaId).and.eq(column.infoNo, entity.infoNo).and.eq(column.suffix, entity.suffix).and.eq(column.imageName, entity.imageName).and.eq(column.version, entity.version)
    }.update().apply()
  }

}

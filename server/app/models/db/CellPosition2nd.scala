package models.db

import com.ponkotuy.data.master

import scalikejdbc._

case class CellPosition2nd(
  areaId: Int,
  infoNo: Int,
  cell: Int,
  posX: Int,
  posY: Int,
  version: Int) {

  def save()(implicit session: DBSession = CellPosition2nd.autoSession): CellPosition2nd = CellPosition2nd.save(this)(session)

  def destroy()(implicit session: DBSession = CellPosition2nd.autoSession): Unit = CellPosition2nd.destroy(this)(session)

}


object CellPosition2nd extends SQLSyntaxSupport[CellPosition2nd] {

  override val tableName = "cell_position_2nd"

  override val columns = Seq("area_id", "info_no", "cell", "pos_x", "pos_y", "version")

  def apply(cp: SyntaxProvider[CellPosition2nd])(rs: WrappedResultSet): CellPosition2nd = autoConstruct(rs, cp)
  def apply(cp: ResultName[CellPosition2nd])(rs: WrappedResultSet): CellPosition2nd = autoConstruct(rs, cp)

  val cp = CellPosition2nd.syntax("cp")

  override val autoSession = AutoSession

  def find(areaId: Int, cell: Int, infoNo: Int, version: Int = 0)(implicit session: DBSession = autoSession): Option[CellPosition2nd] = {
    if(version != 0) findWithVersion(areaId, cell, infoNo, version)
    else {
      withSQL {
        select.from(CellPosition2nd as cp)
            .where.eq(cp.areaId, areaId).and.eq(cp.cell, cell).and.eq(cp.infoNo, infoNo)
            .orderBy(cp.version.desc).limit(1)
      }.map(CellPosition2nd(cp.resultName)).single().apply()
    }
  }

  private def findWithVersion(areaId: Int, cell: Int, infoNo: Int, version: Int = 0)(implicit session: DBSession = autoSession): Option[CellPosition2nd] = {
    withSQL {
      select.from(CellPosition2nd as cp)
          .where.eq(cp.areaId, areaId).and.eq(cp.cell, cell).and.eq(cp.infoNo, infoNo).and.eq(cp.version, version)
    }.map(CellPosition2nd(cp.resultName)).single().apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[CellPosition2nd] = {
    withSQL(select.from(CellPosition2nd as cp)).map(CellPosition2nd(cp.resultName)).list().apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(CellPosition2nd as cp)).map(rs => rs.long(1)).single().apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[CellPosition2nd] = {
    withSQL {
      select.from(CellPosition2nd as cp).where.append(where)
    }.map(CellPosition2nd(cp.resultName)).single().apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[CellPosition2nd] = {
    withSQL {
      select.from(CellPosition2nd as cp).where.append(where)
    }.map(CellPosition2nd(cp.resultName)).list().apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(CellPosition2nd as cp).where.append(where)
    }.map(_.long(1)).single().apply().get
  }

  def create(cp: master.CellPosition)(implicit session: DBSession = autoSession): CellPosition2nd = {
    withSQL {
      insert.into(CellPosition2nd).namedValues(
        column.areaId -> cp.areaId, column.infoNo -> cp.infoNo,
        column.cell -> cp.cell, column.posX -> cp.posX, column.posY -> cp.posY,
        column.version -> cp.version
      )
    }.update().apply()
    CellPosition2nd(
      cp.areaId,
      cp.infoNo,
      cp.cell,
      cp.posX,
      cp.posY,
      cp.version)
  }

  def bulkInsert(ss: Seq[master.CellPosition], memberId: Long)(
      implicit session: DBSession = autoSession): Unit = {
    applyUpdate {
      insert.into(CellPosition2nd).columns(
        column.areaId,
        column.infoNo,
        column.cell,
        column.posX,
        column.posY,
        column.version
      ).multiValues(
          cp.map(_.areaId), cp.map(_.infoNo),
          cp.map(_.cell), cp.map(_.posX), cp.map(_.posY),
          cp.map(_.version)
        )
    }
  }


  def save(entity: CellPosition2nd)(implicit session: DBSession = autoSession): CellPosition2nd = {
    withSQL {
      update(CellPosition2nd).set(
        column.areaId -> entity.areaId,
        column.infoNo -> entity.infoNo,
        column.cell -> entity.cell,
        column.posX -> entity.posX,
        column.posY -> entity.posY
      ).where.eq(column.areaId, entity.areaId).and.eq(column.cell, entity.cell).and.eq(column.infoNo, entity.infoNo).and.eq(column.version, entity.version)
    }.update().apply()
    entity
  }

  def destroy(entity: CellPosition2nd)(implicit session: DBSession = autoSession): Unit = {
    withSQL {
      delete.from(CellPosition2nd).where.eq(column.areaId, entity.areaId).and.eq(column.cell, entity.cell).and.eq(column.infoNo, entity.infoNo).and.eq(column.version, entity.version)
    }.update().apply()
  }

}

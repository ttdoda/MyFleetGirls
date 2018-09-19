package models.db

import scalikejdbc._

case class ShipImage2nd(
    id: Int,
    image: Array[Byte],
    memberId: Long,
    kind: String,
    version: Int) {

  def save()(implicit session: DBSession = ShipImage2nd.autoSession): ShipImage2nd = ShipImage2nd.save(this)(session)

  def destroy()(implicit session: DBSession = ShipImage2nd.autoSession): Unit = ShipImage2nd.destroy(this)(session)

}

object ShipImage2nd extends SQLSyntaxSupport[ShipImage2nd] {

  override val tableName = "ship_image_2nd"

  override val columns = Seq("id", "image", "member_id", "kind", "version")

  def apply(si: ResultName[ShipImage2nd])(rs: WrappedResultSet): ShipImage2nd = autoConstruct(rs, si)

  val si = ShipImage2nd.syntax("si")
  val a = Admiral.syntax("a")

  override val autoSession = AutoSession

  def find(id: Int, kind: String, ver: Int = 0)(implicit session: DBSession = autoSession): Option[ShipImage2nd] = {
    if(ver != 0) findWithVersion(id, kind, ver)
    else {
      withSQL {
        select.from(ShipImage2nd as si)
            .where.eq(si.id, id).and.eq(si.kind, kind)
            .orderBy(si.version.desc).limit(1)
      }.map(ShipImage2nd(si.resultName)).single().apply()
    }
  }

  private def findWithVersion(id: Int, kind: String, ver: Int)(implicit session: DBSession = autoSession): Option[ShipImage2nd] = {
    withSQL {
      select.from(ShipImage2nd as si)
          .where.eq(si.id, id).and.eq(si.kind, kind).and.eq(si.version, ver)
    }.map(ShipImage2nd(si.resultName)).single().apply()
  }

  def findAdmiralOfCard(sid: Int)(implicit session: DBSession = autoSession): Option[Admiral] = withSQL {
    select(a.resultAll).from(ShipImage2nd as si)
        .innerJoin(Admiral as a).on(si.memberId, a.id)
        .where.eq(si.id, sid).and.eq(si.kind, "card")
        .orderBy(si.version.desc).limit(1)
  }.map(Admiral(a)).single().apply()

  def findAdmiralOfCardDmg(sid: Int)(implicit session: DBSession = autoSession): Option[Admiral] = withSQL {
    select(a.resultAll).from(ShipImage2nd as si)
        .innerJoin(Admiral as a).on(si.memberId, a.id)
        .where.eq(si.id, sid).and.eq(si.kind, "card_dmg")
        .orderBy(si.version.desc).limit(1)
  }.map(Admiral(a)).single().apply()

  def findAll()(implicit session: DBSession = autoSession): List[ShipImage2nd] = {
    withSQL(select.from(ShipImage2nd as si)).map(ShipImage2nd(si.resultName)).list().apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls"count(1)").from(ShipImage2nd as si)).map(rs => rs.long(1)).single().apply().get
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[ShipImage2nd] = {
    withSQL {
      select.from(ShipImage2nd as si).where.append(sqls"${where}")
    }.map(ShipImage2nd(si.resultName)).list().apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls"count(1)").from(ShipImage2nd as si).where.append(sqls"${where}")
    }.map(_.long(1)).single().apply().get
  }

  def create(
      id: Int,
      image: Array[Byte],
      memberId: Long,
      kind: String,
      version: Int)(implicit session: DBSession = autoSession): Unit = {
    withSQL {
      insert.into(ShipImage2nd).columns(
        column.id,
        column.image,
        column.memberId,
        column.kind,
        column.version
      ).values(
          id,
          image,
          memberId,
          kind,
          version
        )
    }.update().apply()
  }

  def save(entity: ShipImage2nd)(implicit session: DBSession = autoSession): ShipImage2nd = {
    withSQL {
      update(ShipImage2nd).set(
        column.image -> entity.image,
        column.memberId -> entity.memberId
      ).where.eq(column.id, entity.id).and.eq(column.kind, entity.kind).and.eq(column.version, entity.version)
    }.update().apply()
    entity
  }

  def destroy(entity: ShipImage2nd)(implicit session: DBSession = autoSession): Unit = {
    withSQL {
      delete.from(ShipImage2nd)
          .where.eq(column.id, entity.id).and.eq(column.kind, entity.kind).and.eq(column.version, entity.version)
    }.update().apply()
  }

}

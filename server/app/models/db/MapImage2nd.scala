package models.db

import scalikejdbc._

case class MapImage2nd(
  areaId: Int,
  infoNo: Int,
  suffix: Int,
  image: Array[Byte],
  version: Short) {

  def save()(implicit session: DBSession = MapImage2nd.autoSession): MapImage2nd = MapImage2nd.save(this)(session)

  def destroy()(implicit session: DBSession = MapImage2nd.autoSession): Unit = MapImage2nd.destroy(this)(session)

}


object MapImage2nd extends SQLSyntaxSupport[MapImage2nd] {

  override val tableName = "map_image_2nd"

  override val columns = Seq("area_id", "info_no", "suffix", "image", "version")

  def apply(mi: SyntaxProvider[MapImage2nd])(rs: WrappedResultSet): MapImage2nd = autoConstruct(rs, mi)
  def apply(mi: ResultName[MapImage2nd])(rs: WrappedResultSet): MapImage2nd = autoConstruct(rs, mi)

  val mi = MapImage2nd.syntax("mi")

  override val autoSession = AutoSession

  def find(areaId: Int, infoNo: Int, suffix: Int, version: Short = 0)(implicit session: DBSession = autoSession): Option[MapImage2nd] = {
    if(version != 0) findWithVersion(areaId, infoNo, suffix, version)
    else {
      withSQL {
        select.from(MapImage2nd as mi)
            .where.eq(mi.areaId, areaId).and.eq(mi.infoNo, infoNo).and.eq(mi.suffix, suffix)
            .orderBy(mi.version.desc).limit(1)
      }.map(MapImage2nd(mi.resultName)).single().apply()
    }
  }

  private def findWithVersion(areaId: Int, infoNo: Int, suffix: Int, version: Short)(implicit session: DBSession = autoSession): Option[MapImage2nd] = {
    withSQL {
      select.from(MapImage2nd as mi).where.eq(mi.areaId, areaId).and.eq(mi.infoNo, infoNo).and.eq(mi.suffix, suffix).and.eq(mi.version, version)
    }.map(MapImage2nd(mi.resultName)).single().apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[MapImage2nd] = {
    withSQL(select.from(MapImage2nd as mi)).map(MapImage2nd(mi.resultName)).list().apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(MapImage2nd as mi)).map(rs => rs.long(1)).single().apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[MapImage2nd] = {
    withSQL {
      select.from(MapImage2nd as mi).where.append(where)
    }.map(MapImage2nd(mi.resultName)).single().apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[MapImage2nd] = {
    withSQL {
      select.from(MapImage2nd as mi).where.append(where)
    }.map(MapImage2nd(mi.resultName)).list().apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(MapImage2nd as mi).where.append(where)
    }.map(_.long(1)).single().apply().get
  }

  def create(
    areaId: Int,
    infoNo: Int,
    suffix: Int,
    image: Array[Byte],
    version: Short)(implicit session: DBSession = autoSession): MapImage2nd = {
    withSQL {
      insert.into(MapImage2nd).columns(
        column.areaId,
        column.infoNo,
        column.suffix,
        column.image,
        column.version
      ).values(
            areaId,
            infoNo,
            suffix,
            image,
            version
          )
    }.update().apply()

    MapImage2nd(
      areaId = areaId,
      infoNo = infoNo,
      suffix = suffix,
      image = image,
      version = version)
  }

  def save(entity: MapImage2nd)(implicit session: DBSession = autoSession): MapImage2nd = {
    withSQL {
      update(MapImage2nd).set(
        column.areaId -> entity.areaId,
        column.infoNo -> entity.infoNo,
        column.suffix -> entity.suffix,
        column.image -> entity.image
      ).where.eq(column.areaId, entity.areaId).and.eq(column.infoNo, entity.infoNo).and.eq(column.suffix, entity.suffix).and.eq(column.version, entity.version)
    }.update().apply()
    entity
  }

  def destroy(entity: MapImage2nd)(implicit session: DBSession = autoSession): Unit = {
    withSQL {
      delete.from(MapImage2nd).where.eq(column.areaId, entity.areaId).and.eq(column.infoNo, entity.infoNo).and.eq(column.suffix, entity.suffix).and.eq(column.version, entity.version)
    }.update().apply()
  }

}

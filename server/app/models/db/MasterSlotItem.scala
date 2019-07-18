package models.db

import com.ponkotuy.data.master
import scalikejdbc._
import tool.{EquipIconType, EquipType}
import util.scalikejdbc.BulkInsert._

case class MasterSlotItem(
  id: Int,
  name: String,
  typ: Array[Int],
  power: Int,
  torpedo: Int,
  bomb: Int,
  antiair: Int,
  antisub: Int,
  search: Int,
  hit: Int,
  length: Int,
  rare: Int) {

  def save()(implicit session: DBSession = MasterSlotItem.autoSession): MasterSlotItem = MasterSlotItem.save(this)(session)

  def destroy()(implicit session: DBSession = MasterSlotItem.autoSession): Unit = MasterSlotItem.destroy(this)(session)

  /** typのうち3番目の値より。種別 */
  def category: Option[EquipType] = typ.lift(2).flatMap(EquipType.fromInt)

  /** typeの4番目の値より。アイコン色の元となっている種別 */
  def iconType: Option[EquipIconType] = typ.lift(3).flatMap(EquipIconType.fromInt)

}


object MasterSlotItem extends SQLSyntaxSupport[MasterSlotItem] {

  override val tableName = "master_slot_item"

  override val columns = Seq("id", "name", "typ", "power", "torpedo", "bomb", "antiAir", "antiSub", "search", "hit", "length", "rare")

  def apply(msi: SyntaxProvider[MasterSlotItem])(rs: WrappedResultSet): MasterSlotItem = apply(msi.resultName)(rs)
  def apply(msi: ResultName[MasterSlotItem])(rs: WrappedResultSet): MasterSlotItem = new MasterSlotItem(
    id = rs.int(msi.id),
    name = rs.string(msi.name),
    typ = rs.string(msi.typ).split(',').map(_.toInt),
    power = rs.int(msi.power),
    torpedo = rs.int(msi.torpedo),
    bomb = rs.int(msi.bomb),
    antiair = rs.int(msi.antiair),
    antisub = rs.int(msi.antisub),
    search = rs.int(msi.search),
    hit = rs.int(msi.hit),
    length = rs.int(msi.length),
    rare = rs.int(msi.rare),
  )

  val msi = MasterSlotItem.syntax("msi")

  override val autoSession = AutoSession

  def find(id: Int)(implicit session: DBSession = autoSession): Option[MasterSlotItem] = {
    withSQL {
      select.from(MasterSlotItem as msi).where.eq(msi.id, id)
    }.map(MasterSlotItem(msi.resultName)).single().apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[MasterSlotItem] = {
    withSQL(select.from(MasterSlotItem as msi)).map(MasterSlotItem(msi.resultName)).list().apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls"count(1)").from(MasterSlotItem as msi)).map(rs => rs.long(1)).single().apply().get
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[MasterSlotItem] = {
    withSQL {
      select.from(MasterSlotItem as msi).where.append(sqls"${where}")
    }.map(MasterSlotItem(msi.resultName)).list().apply()
  }

  def findAllName()(implicit session: DBSession = autoSession): List[String] = withSQL {
    select(msi.name).from(MasterSlotItem as msi)
  }.map(_.string(1)).list().apply()

  def findIn(items: Seq[Int])(implicit session: DBSession = autoSession): List[MasterSlotItem] = {
    items match {
      case Seq() => Nil
      case _ =>
        withSQL {
          select.from(MasterSlotItem as msi)
            .where.in(msi.id, items)
        }.map(MasterSlotItem(msi.resultName)).list().apply()
    }
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls"count(1)").from(MasterSlotItem as msi).where.append(sqls"${where}")
    }.map(_.long(1)).single().apply().get
  }

  def create(
    id: Int,
    name: String,
    typ: List[Int],
    power: Int,
    torpedo: Int,
    bomb: Int,
    antiair: Int,
    antisub: Int,
    search: Int,
    hit: Int,
    length: Int,
    rare: Int)(implicit session: DBSession = autoSession): MasterSlotItem = {
    withSQL {
      insert.into(MasterSlotItem).columns(
        column.id,
        column.name,
        column.typ,
        column.power,
        column.torpedo,
        column.bomb,
        column.antiair,
        column.antisub,
        column.search,
        column.hit,
        column.length,
        column.rare
      ).values(
          id,
          name,
          typ.mkString(","),
          power,
          torpedo,
          bomb,
          antiair,
          antisub,
          search,
          hit,
          length,
          rare
        )
    }.update().apply()

    MasterSlotItem(
      id = id,
      name = name,
      typ = typ.toArray,
      power = power,
      torpedo = torpedo,
      bomb = bomb,
      antiair = antiair,
      antisub = antisub,
      search = search,
      hit = hit,
      length = length,
      rare = rare)
  }

  def bulkInsert(xs: Seq[master.MasterSlotItem])(implicit session: DBSession = autoSession): Seq[MasterSlotItem] = {
    applyUpdate {
      insert.into(MasterSlotItem)
        .columns(column.id, column.name, column.typ,
          column.power, column.torpedo, column.bomb, column.antiair, column.antisub,
          column.search, column.hit, column.length, column.rare)
        .multiValues(xs.map(_.id), xs.map(_.name), xs.map(_.typ.mkString(",")),
          xs.map(_.power), xs.map(_.torpedo), xs.map(_.bomb), xs.map(_.antiAir), xs.map(_.antiSub),
          xs.map(_.search), xs.map(_.hit), xs.map(_.length), xs.map(_.rare))
    }
    xs.map { x =>
      MasterSlotItem(x.id, x.name, x.typ.toArray, x.power, x.torpedo, x.bomb, x.antiAir, x.antiSub,
        x.search, x.hit, x.length, x.rare)
    }
  }

  def save(entity: MasterSlotItem)(implicit session: DBSession = autoSession): MasterSlotItem = {
    withSQL {
      update(MasterSlotItem).set(
        column.id -> entity.id,
        column.name -> entity.name,
        column.typ -> entity.typ.mkString(","),
        column.power -> entity.power,
        column.torpedo -> entity.torpedo,
        column.bomb -> entity.bomb,
        column.antiair -> entity.antiair,
        column.antisub -> entity.antisub,
        column.search -> entity.search,
        column.hit -> entity.hit,
        column.length -> entity.length,
        column.rare -> entity.rare
      ).where.eq(column.id, entity.id)
    }.update().apply()
    entity
  }

  def destroy(entity: MasterSlotItem)(implicit session: DBSession = autoSession): Unit = {
    withSQL {
      delete.from(MasterSlotItem).where.eq(column.id, entity.id)
    }.update().apply()
  }

  def deleteAll()(implicit session: DBSession = autoSession): Unit = applyUpdate {
    delete.from(MasterSlotItem)
  }

}

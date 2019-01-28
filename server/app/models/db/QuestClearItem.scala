package models.db

import com.ponkotuy.data
import scalikejdbc._
import util.scalikejdbc.BulkInsert._

case class QuestClearItem(
  id: Long,
  memberId: Long,
  questId: Int,
  bounusCount: Int,
  `type`: Int,
  count: Int,
  itemId: Option[Int] = None,
  itemName: Option[String] = None,
  created: Long) {

  def save()(implicit session: DBSession = QuestClearItem.autoSession): QuestClearItem = QuestClearItem.save(this)(session)

  def destroy()(implicit session: DBSession = QuestClearItem.autoSession): Int = QuestClearItem.destroy(this)(session)

}


object QuestClearItem extends SQLSyntaxSupport[QuestClearItem] {

  override val tableName = "quest_clear_item"

  override val columns = Seq("id", "member_id", "quest_id", "bounus_count", "type", "count", "item_id", "item_name", "created")

  def apply(qci: SyntaxProvider[QuestClearItem])(rs: WrappedResultSet): QuestClearItem = autoConstruct(rs, qci)
  def apply(qci: ResultName[QuestClearItem])(rs: WrappedResultSet): QuestClearItem = autoConstruct(rs, qci)

  val qci = QuestClearItem.syntax("qci")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[QuestClearItem] = {
    withSQL {
      select.from(QuestClearItem as qci).where.eq(qci.id, id)
    }.map(QuestClearItem(qci.resultName)).single.apply()
  }

  def findAllByUserAndBounusType(memberId: Long, bounusType: Int)(implicit session: DBSession = autoSession): List[QuestClearItem] = {
    withSQL {
      select.from(QuestClearItem as qci)
        .where.eq(qci.memberId, memberId).and.eq(qci.`type`, bounusType)
        .orderBy(qci.created).desc
    }.map(QuestClearItem(qci.resultName)).list.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[QuestClearItem] = {
    withSQL(select.from(QuestClearItem as qci)).map(QuestClearItem(qci.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(QuestClearItem as qci)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[QuestClearItem] = {
    withSQL {
      select.from(QuestClearItem as qci).where.append(where)
    }.map(QuestClearItem(qci.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[QuestClearItem] = {
    withSQL {
      select.from(QuestClearItem as qci).where.append(where)
    }.map(QuestClearItem(qci.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(QuestClearItem as qci).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    memberId: Long,
    questId: Int,
    bounusCount: Int,
    `type`: Int,
    count: Int,
    itemId: Option[Int] = None,
    itemName: Option[String] = None,
    created: Long)(implicit session: DBSession = autoSession): QuestClearItem = {
    val generatedKey = withSQL {
      insert.into(QuestClearItem).namedValues(
        column.memberId -> memberId,
        column.questId -> questId,
        column.bounusCount -> bounusCount,
        column.`type` -> `type`,
        column.count -> count,
        column.itemId -> itemId,
        column.itemName -> itemName,
        column.created -> created
      )
    }.updateAndReturnGeneratedKey.apply()

    QuestClearItem(
      id = generatedKey,
      memberId = memberId,
      questId = questId,
      bounusCount = bounusCount,
      `type` = `type`,
      count = count,
      itemId = itemId,
      itemName = itemName,
      created = created)
  }

  def batchInsert(entities: Seq[QuestClearItem])(implicit session: DBSession = autoSession): List[Int] = {
    val params: Seq[Seq[(Symbol, Any)]] = entities.map(entity =>
      Seq(
        'memberId -> entity.memberId,
        'questId -> entity.questId,
        'bounusCount -> entity.bounusCount,
        'type -> entity.`type`,
        'count -> entity.count,
        'itemId -> entity.itemId,
        'itemName -> entity.itemName,
        'created -> entity.created))
    SQL("""insert into quest_clear_item(
      member_id,
      quest_id,
      bounus_count,
      type,
      count,
      item_id,
      item_name,
      created
    ) values (
      {memberId},
      {questId},
      {bounusCount},
      {type},
      {count},
      {itemId},
      {itemName},
      {created}
    )""").batchByName(params: _*).apply[List]()
  }

  def bulkInsert(qci: data.QuestClearItem, memberId: Long)(implicit session: DBSession = autoSession): Unit = {
    val created = System.currentTimeMillis()
    val bounusCount = qci.bounusCount
    applyUpdate {
      insert.into(QuestClearItem).columns(
        column.memberId,
        column.questId,
        column.bounusCount,
        column.`type`,
        column.count,
        column.itemId,
        column.itemName,
        column.created
      ).multiValues(
          Seq.fill(bounusCount)(memberId),
          Seq.fill(bounusCount)(qci.id),
          Seq.fill(bounusCount)(bounusCount),
          qci.bounus.map(_.bounusType),
          qci.bounus.map(_.count),
          qci.bounus.map(_.item.map(_.id)),
          qci.bounus.map(_.item.map(_.name)),
          Seq.fill(bounusCount)(created)
        )
    }
  }

  def save(entity: QuestClearItem)(implicit session: DBSession = autoSession): QuestClearItem = {
    withSQL {
      update(QuestClearItem).set(
        column.id -> entity.id,
        column.memberId -> entity.memberId,
        column.questId -> entity.questId,
        column.bounusCount -> entity.bounusCount,
        column.`type` -> entity.`type`,
        column.count -> entity.count,
        column.itemId -> entity.itemId,
        column.itemName -> entity.itemName,
        column.created -> entity.created
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: QuestClearItem)(implicit session: DBSession = autoSession): Int = {
    withSQL { delete.from(QuestClearItem).where.eq(column.id, entity.id) }.update.apply()
  }

}

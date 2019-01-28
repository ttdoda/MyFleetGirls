package com.ponkotuy.data

import org.json4s._

/**
 *
 * @author kPherox
 * Date: 19/01/28.
 */
case class QuestClearItem(
    id: Int,
    material: QuestMaterial,
    bounusCount: Int,
    bounus: List[QuestBounus])

object QuestClearItem {
  implicit val formats = DefaultFormats

  def fromJson(obj: JValue, req: Map[String, String]): Option[QuestClearItem] = {
    val JArray(rawMaterial) = obj \ "api_material"
    val material = QuestMaterial.fromJson(rawMaterial)
    val bounusCount = (obj \ "api_bounus_count").extract[Int]
    val bounus = QuestBounus.fromJson(obj \ "api_bounus")
    for {
      questId <- req.get("api_quest_id")
    } yield  QuestClearItem(questId.toInt, material, bounusCount, bounus)
  }
}

case class QuestBounus(bounusType: Int, count: Int, item: Option[QuestBounusItem])
case class QuestBounusItem(id: Int, name: String)

object QuestBounus {
  implicit val formats = DefaultFormats
  def fromJson(json: JValue): List[QuestBounus] = {
    json.extractOrElse[List[RawQuestBounus]](Nil).map(_.build)
  }

  private case class RawQuestBounus(
      api_type: Int,
      api_count: Int,
      api_item: Option[RawQuestBounusItem]
  ) {
    def build: QuestBounus = {
      val item = api_item.map(_.build)
      QuestBounus(api_type, api_count, item)
    }
  }
  private case class RawQuestBounusItem(
    api_id: Int,
    api_name: String
  ) {
    def build: QuestBounusItem = QuestBounusItem(api_id, api_name)
  }
}

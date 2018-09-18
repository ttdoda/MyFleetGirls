package com.ponkotuy.data.master

import org.json4s._

/**
 *
 * @author kPherox
 * Date: 18/09/13
 */
case class MapFrame(
  areaId: Int, infoNo: Int, suffix: Int, name: String, posX: Int, posY: Int, width: Int, height: Int, version: Int
)


object MapFrame {
  implicit val formats = DefaultFormats

  def fromJson(json: JValue, areaId: Int, infoNo: Int, suffix: Int, version: Int): List[MapFrame] = {
    val JObject(obj) = json
    obj.map { case JField(name, data) =>
      val frameName = name.stripPrefix(s"map${"%03d".format(areaId)}${"%02d".format(infoNo)}_")
      data.extractOpt[MapFrameData].map(_.frame.build(areaId, infoNo, suffix, frameName, version))
    }.flatten
  }

  private case class MapFrameData(frame: RawMapFrame)

  private case class RawMapFrame(
    x: Int, y: Int, w: Int, h: Int
  ) {
    def build(areaId: Int, infoNo: Int, suffix: Int, name: String, version: Int): MapFrame =
      MapFrame(areaId, infoNo, suffix, name, x, y, w, h, version)
  }
}

case class MapInfo(
  spots: List[CellPosition], bg: List[BackgroundName], enemies: List[EnemiesPosition], labels: List[LabelPosition]
)

object MapInfo {
  implicit val formats = DefaultFormats

  def fromJson(json: JValue, areaId: Int, infoNo: Int, suffix: Int, version: Int): MapInfo = {
    val spots: List[CellPosition] = CellPosition.fromJson(json \ "spots", areaId, infoNo, suffix, version)
    val bg: List[String] = (json \ "bg").extractOrElse[List[JValue]](Nil).map {
      case JString(bg) => bg
      case background => (background \ "img").extract[String]
    }
    val bgName: List[BackgroundName] = bg.zipWithIndex.map { case (nane, priority) =>
      BackgroundName(areaId, infoNo, suffix, priority, nane, version)
    }
    val enemies: List[EnemiesPosition] = EnemiesPosition.fromJson(json \ "enemies", areaId, infoNo, suffix, version)
    val labels: List[LabelPosition] = LabelPosition.fromJson(json \ "labels", areaId, infoNo, suffix, version)
    MapInfo(spots, bgName, enemies, labels)
  }
}

case class RawImageName(
  name: Option[String], img: String
)

case class RawPosition(
  x: Int, y: Int
)

case class CellPosition(
  areaId: Int, infoNo: Int, suffix: Int, cell: Int, posX: Int, posY: Int, routeName: Option[String], routeX: Option[Int], routeY: Option[Int], version: Int
)

object CellPosition {
  implicit val formats = DefaultFormats

  def fromJson(json: JValue, areaId: Int, infoNo: Int, suffix: Int, version: Int): List[CellPosition] = {
    json.extract[List[RawCellPosition]].map(_.build(areaId, infoNo, suffix, version))
  }

  private case class RawCellPosition(
    no: Int, x: Int, y: Int, route: Option[RawImageName], line: Option[RawPosition]
  ) {
    def build(areaId: Int, infoNo: Int, suffix: Int, version: Int): CellPosition =
      CellPosition(areaId, infoNo, suffix, no, x, y, route.map(_.img), line.map(_.x), line.map(_.y), version)
  }
}

case class BackgroundName(
  areaId: Int, infoNo: Int, suffix: Int, priority: Int, imageName: String, version: Int
)

case class EnemiesPosition(
  areaId: Int, infoNo: Int, suffix: Int, cell: Int, imageName: String, posX: Int, posY: Int, version: Int
)

object EnemiesPosition {
  implicit val formats = DefaultFormats

  def fromJson(json: JValue, areaId: Int, infoNo: Int, suffix: Int, version: Int): List[EnemiesPosition] = {
    json.extractOrElse[List[RawEnemiesPosition]](Nil).map(_.build(areaId, infoNo, suffix, version))
  }

  private case class RawEnemiesPosition(
    no: Int, x: Int, y: Int, img: String
  ) {
    def build(areaId: Int, infoNo: Int, suffix: Int, version: Int): EnemiesPosition =
      EnemiesPosition(areaId, infoNo, suffix, no, img, x, y, version)
  }
}

case class LabelPosition(
  areaId: Int, infoNo: Int, suffix: Int, imageName: String, posX: Int, posY: Int, version: Int
)

object LabelPosition {
  implicit val formats = DefaultFormats

  def fromJson(json: JValue, areaId: Int, infoNo: Int, suffix: Int, version: Int): List[LabelPosition] = {
    json.extractOrElse[List[RawLabelPosition]](Nil).map(_.build(areaId, infoNo, suffix, version))
  }

  private case class RawLabelPosition(
    x: Int, y: Int, img: String
  ) {
    def build(areaId: Int, infoNo: Int, suffix: Int, version: Int): LabelPosition =
      LabelPosition(areaId, infoNo, suffix, img, x, y, version)
  }
}

package com.ponkotuy.data.master

import org.json4s._

/**
 *
 * @author kPherox
 * Date: 18/09/13
 */
case class MapFrame(
  areaId: Int, infoNo: Int, name: String, frameX: Int, frameY: Int, frameW: Int, frameH: Int, version: Int
)


object MapFrame {
  implicit val formats = DefaultFormats

  def fromJson(json: JValue,  areaId: Int, infoNo: Int, version: Int): List[MapFrame] = {
    val JObject(obj) = json
    obj.map { case JField(name, data) =>
      val frameName = name.stripPrefix(s"map${"%03d".format(areaId)}${"%02d".format(infoNo)}_")
      data.extractOpt[MapFrameData].map(_.frame.build(areaId, infoNo, frameName, version))
    }.flatten
  }

  private case class MapFrameData(frame: RawMapFrame)

  private case class RawMapFrame(
    x: Int, y: Int, w: Int, h: Int
  ) {
    def build(areaId: Int, infoNo: Int, name: String, version: Int): MapFrame =
      MapFrame(areaId, infoNo, name, x, y, w, h, version)
  }
}

case class CellPosition(
  areaId: Int, infoNo: Int, cell: Int, posX: Int, posY: Int, version: Int
)

object CellPosition {
  implicit val formats = DefaultFormats

  def fromJson(json: JValue, areaId: Int, infoNo: Int, version: Int): List[CellPosition] = {
    json.extractOrElse[List[RawCellPosition]](Nil).map(_.build(areaId, infoNo, version))
  }

  private case class RawCellPosition(
    no: Int, x: Int, y: Int
  ) {
    def build(areaId: Int, infoNo: Int, version: Int): CellPosition =
      CellPosition(areaId, infoNo, no, x, y, version)
  }
}

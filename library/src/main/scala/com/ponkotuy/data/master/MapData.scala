package com.ponkotuy.data.master

import org.json4s._

/**
 *
 * @author kPherox
 * Date: 18/09/13
 */
case class MapData(
  areaId: Int, infoNo: Int, name: String, frameX: Int, frameY: Int, frameW: Int, frameH: Int, version: Int
)


object MapData {
  //def fromJson(json: JValue,  areaId: Int, infoNo: Int, version: Int): List[MapData]
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

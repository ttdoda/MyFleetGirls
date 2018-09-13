package com.ponkotuy.restype

import com.ponkotuy.data.master

import com.netaporter.uri.Uri
import com.ponkotuy.http.MFGHttp
import com.ponkotuy.parser.Query

import scala.util.Try
import scala.util.matching.Regex

import org.json4s.native.Serialization.write

/**
 * @author kPherox
 * DAte: 18/09/12.
 */
trait MapData {
  def regexp: Regex

  def parse(uri: Uri): Option[(Int, Int)] = {
    Try {
      uri.path match {
        case this.regexp(id, no) => (id.toInt, no.toInt)
      }
    }.toOption
  }

  def extractNumber(str: String): Option[Int] = Try {
    str.filter { c => '0' <= c && c <= '9' }.toInt
  }.toOption
}

/**
 * Map image
 */
object MapImage extends ResType with Resources with Media with MapData {
  def regexp: Regex = """\A/kcs2/resources/map/(\d+)/(\d+)_image.png\z""".r

  def postables(q: Query): Seq[Result] = {
    val ver = q.uri.query.param("version").flatMap(extractNumber).getOrElse(DefaultVer)
    parse(q.uri).filterNot { case (areaId, infoNo) => MFGHttp.existsMap(areaId, infoNo, ver) }.map { case (areaId, infoNo) =>
      val png = allRead(q.responseContent)
      FilePostable(s"/image/map/${areaId}/${infoNo}/${ver}", "map", 2, png, "png")
    }.toList
  }
}

/**
 * Map image sprite data
 */
object MapImageSprite extends ResType with Resources with MapData {
  def regexp: Regex = """\A/kcs2/resources/map/(\d+)/(\d+)_image.json\z""".r

  def postables(q: Query): Seq[Result] = {
    val ver = q.uri.query.param("version").flatMap(extractNumber).getOrElse(DefaultVer)
    parse(q.uri).map { case (areaId, infoNo) =>
      //val result = master.MapData.fromJson(json, areaId, infoNo, ver)
      //NormalPostable(s"/map_data", write(result)) :: Nil
    }.getOrElse(Nil)
  }
}

/**
 * Map image cell position
 */
object MapImageInfo extends ResType with Resources with MapData {
  def regexp: Regex = """\A/kcs2/resources/map/(\d+)/(\d+)_info.json\z""".r

  def postables(q: Query): Seq[Result] = {
    val ver = q.uri.query.param("version").flatMap(extractNumber).getOrElse(DefaultVer)
    parse(q.uri).map { case (areaId, infoNo) =>
      //val result = master.CellPosition.fromJson(json, areaId, infoNo, ver)
      //NormalPostable(s"/cell_position", write(result)) :: Nil
    }.getOrElse(Nil)
  }
}

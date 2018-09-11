package com.ponkotuy.restype

import com.ponkotuy.http.MFGHttp
import com.ponkotuy.parser.Query

import scala.util.matching.Regex

/**
 * @author kPherox
 * Date: 18/09/10.
 */
case object ShipImage extends Resources {
  def regexp: Regex = """\A/kcs2/resources/ship/(.*)/(\d+)_\d+.png\z""".r

  def postables(q: Query): Seq[Result] = {
    val ver = q.uri.query.param("version").map(_.toInt).getOrElse(DefaultVer)
    parse(q.uri).filterNot { case (shipId, kind) => MFGHttp.existsImage(shipId, kind, ver) }.map { case (shipId, kind) =>
      val png = allRead(q.responseContent)
      FilePostable(s"/image/ship/${shipId}/${kind}/${ver}", "image", 2, png, "png")
    }.toList
  }
}

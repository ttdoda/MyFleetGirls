package com.ponkotuy.restype

import com.netaporter.uri.Uri
import com.ponkotuy.http.MFGHttp
import com.ponkotuy.parser.Query

import scala.util.Try
import scala.util.matching.Regex

/**
 * @author kPherox
 * Date: 18/09/10.
 */
case object ShipImage extends ResType with Resources with Media {
  def regexp: Regex = """\A/kcs2/resources/ship/(.*)/(\d+)_\d+.png\z""".r

  def postables(q: Query): Seq[Result] = {
    val ver = q.uri.query.param("version").map(_.toInt).getOrElse(DefaultVer)
    parse(q.uri).filterNot { case (shipId, kind) => MFGHttp.existsImage(shipId, kind, ver) }.map { case (shipId, kind) =>
      val png = allRead(q.responseContent)
      FilePostable(s"/image/ship/${shipId}/${kind}/${ver}", "image", 2, png, "png")
    }.toList
  }

  private def parse(uri: Uri): Option[(Int, String)] = {
    Try {
      uri.path match {
        case this.regexp(name, id) => (id.toInt, name)
      }
    }.toOption
  }
}

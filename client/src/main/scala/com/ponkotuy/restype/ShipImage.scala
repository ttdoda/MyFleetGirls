package com.ponkotuy.restype

import com.netaporter.uri.Uri
import com.ponkotuy.http.MFGHttp
import com.ponkotuy.parser.Query
import io.netty.buffer.ByteBuf

import scala.util.Try
import scala.util.matching.Regex

/**
 * @author kPherox
 * Date: 18/09/10.
 */
case object ShipImage extends ResType {
  val DefaultVer = 0
  val Pattern = """.*/kcs2/resources/ship/(.*)/(\d+)_\d+.png""".r

  override def regexp: Regex = """\A/kcs2/resources/ship/(.*)/\d+_\d+.png\z""".r

  override def postables(q: Query): Seq[Result] = {
    val ver = q.uri.query.param("version").map(_.toInt).getOrElse(DefaultVer)
    parse(q.uri).filterNot { case (shipId, kind) => MFGHttp.existsImage2nd(shipId, kind, ver) }.map { case (shipId, kind) =>
      val png = allRead(q.responseContent)
      FilePostable(s"/image/ship/$shipId/$kind/$ver", "image", 2, png, "png")
    }.toList
  }

  private def parse(uri: Uri): Option[(Int, String)] = {
    Try {
      uri.path match {
        case Pattern(kind, id) => (id.toInt, kind)
      }
    }.toOption
  }

  def allRead(buf: ByteBuf): Array[Byte] = {
    val arr = new Array[Byte](buf.readableBytes())
    buf.getBytes(buf.readerIndex(), arr)
    arr
  }
}

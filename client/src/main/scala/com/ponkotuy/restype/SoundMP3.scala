package com.ponkotuy.restype

import io.lemonlabs.uri.Uri
import com.ponkotuy.http.MFGHttp
import com.ponkotuy.parser.Query

import scala.util.Try
import scala.util.matching.Regex

/**
 * @author ponkotuy
 * DAte: 15/04/12.
 */
case object SoundMP3 extends ResType with Resources with Media {
  def regexp: Regex = """\A/kcs/sound/kc([a-z]+)/(\d+).mp3""".r

  def postables(q: Query): Seq[Result] = {
    val ver = q.uri.toUrl.query.param("version").map(_.toInt).getOrElse(DefaultVer)
    parse(q.uri).filterNot { case (soundId, shipKey) => MFGHttp.existsSound(shipKey, soundId, ver) }.map { case (soundId, shipKey) =>
      val sound = allRead(q.responseContent)
      FilePostable(s"/mp3/kc/${shipKey}/${soundId}/${ver}", "sound", 2, sound, "mp3")
    }.toList
  }

  private def parse(uri: Uri): Option[(Int, String)] = {
    Try {
      uri.path.toStringRaw match {
        case this.regexp(name, id) => (id.toInt, name)
      }
    }.toOption
  }
}

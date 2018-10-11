package com.ponkotuy.restype

import io.lemonlabs.uri.Uri
import com.ponkotuy.config.ClientConfig
import com.ponkotuy.data
import com.ponkotuy.data.{Auth, MyFleetAuth}
import com.ponkotuy.http.MFGHttp
import com.ponkotuy.parser.Query
import com.ponkotuy.value.KCServer
import com.ponkotuy.util.Log
import org.json4s._
import org.json4s.native.Serialization._

import scala.util.matching.Regex

/**
 * @author ponkotuy
 * Date: 15/04/12.
 */
case object Basic extends ResType with Log {
  import ResType._

  private[this] var memberId: Option[Long] = None
  private[this] var nickname: Option[String] = None
  private[this] var initSending = false

  override def regexp: Regex = s"\\A$GetMember/basic\\z".r

  override def postables(q: Query): Seq[Result] = postablesFromObj(q.obj, q.uri)

  def postablesFromObj(obj: JValue, uri: Uri): Seq[Result] = {
    val auth = data.Auth.fromJSON(obj)
    if(memberId.exists(_ != auth.memberId)) {
      logger.error("Invalid KC User connection. authed:{}, request:{}",auth.memberId,memberId)
      System.err.println("異なる艦これアカウントによる通信を検知しました。一旦終了します")
      System.exit(1) // 例外が伝搬するか自信が無かったので問答無用で殺す
    }
    memberId = Some(auth.memberId)
    nickname = Some(auth.nickname)
    val auth2 = ClientConfig.auth(auth.memberId)

    if(!initSending) postAdmiralSettings(uri)(Some(auth), auth2)

    val basic = data.Basic.fromJSON(obj)

    NormalPostable("/basic", write(basic), 1, basic.summary) :: Authentication(auth, auth2) :: Nil
  }

  private def postAdmiralSettings(uri: Uri)(implicit auth: Option[Auth], auth2: Option[MyFleetAuth]): Unit = {
    for {
      host <- uri.toUrl.hostOption
      kcServer <- KCServer.fromIP(host.value)
    } {
      MFGHttp.post("/admiral_settings", write(kcServer))(auth, auth2)
      println(s"所属： ${kcServer.name}")
      initSending = true
    }
  }

  def getMemberId: Option[Long] = memberId
  def getNickname: Option[String] = nickname
}

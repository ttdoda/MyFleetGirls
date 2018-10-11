package com.ponkotuy.http

import io.lemonlabs.uri.Uri
import com.ponkotuy.value.KCServer

/**
 * @author ponkotuy
 * Date: 15/04/12.
 */
object Host {
  private[this] var kcServer: Option[KCServer] = None
  def get(): Option[KCServer] = kcServer
  def set(uri: Uri): Unit = {
    if(kcServer.isEmpty) {
      val opt = for {
        host <- uri.toUrl.hostOption
        server <- KCServer.fromIP(host.value)
      } yield server
      kcServer = opt
    }
  }
}

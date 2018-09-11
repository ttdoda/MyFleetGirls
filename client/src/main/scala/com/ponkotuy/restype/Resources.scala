package com.ponkotuy.restype

import com.netaporter.uri.Uri
import io.netty.buffer.ByteBuf

import scala.util.Try

/**
 * @author kPherox
 * Date: 18/09/12.
 */
abstract class Resources extends ResType {
  val DefaultVer: Int = 0

  def parse(uri: Uri): Option[(Int, String)] = {
    Try {
      uri.path match {
        case this.regexp(name, id) => (id.toInt, name)
      }
    }.toOption
  }

  def allRead(buf: ByteBuf): Array[Byte] = {
    val arr = new Array[Byte](buf.readableBytes())
    buf.getBytes(buf.readerIndex(), arr)
    arr
  }
}

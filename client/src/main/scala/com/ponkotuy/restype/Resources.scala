package com.ponkotuy.restype

import io.netty.buffer.ByteBuf

import scala.util.Try

/**
 * @author kPherox
 * Date: 18/09/12.
 */
trait Resources {
  val DefaultVer: Int = 0
}

trait Media {
  def allRead(buf: ByteBuf): Array[Byte] = {
    val arr = new Array[Byte](buf.readableBytes())
    buf.getBytes(buf.readerIndex(), arr)
    arr
  }
}

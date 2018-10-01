package com.ponkotuy.intercept

import io.lemonlabs.uri.Uri
import io.netty.buffer.ByteBuf

/**
 *
 * @author ponkotuy
 * Date: 14/02/18.
 */
trait Interceptor {
  def input(uri: Uri, requestContent: ByteBuf, responseContent: ByteBuf): Unit
}

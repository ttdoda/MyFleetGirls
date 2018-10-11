package com.ponkotuy.proxy

import io.lemonlabs.uri.Uri
import com.ponkotuy.intercept.Interceptor
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.{HttpHeaders, HttpRequest}
import org.littleshoot.proxy.{HttpFilters, HttpFiltersAdapter, HttpFiltersSourceAdapter}


class KCFiltersSource(hosts: Set[String], interceptor: Interceptor) extends HttpFiltersSourceAdapter {

  private val noopFilters = new HttpFiltersAdapter(null)

  override def filterRequest(originalRequest: HttpRequest, ctx: ChannelHandlerContext): HttpFilters =
    if (hosts(HttpHeaders.getHost(originalRequest)))
      new AggregateContentFilters(originalRequest, ctx) {
        def finished(requestContent: ByteBuf, responseContent: ByteBuf): Unit = {
          val uri = Uri.parse(originalRequest.getUri)
          interceptor.input(uri, requestContent, responseContent)
        }
      }
    else noopFilters

}

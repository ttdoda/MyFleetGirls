package com.ponkotuy.http

import java.io._
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

import com.ponkotuy.build.BuildInfo
import com.ponkotuy.config.ClientConfig
import com.ponkotuy.data.{Auth, MyFleetAuth}
import com.ponkotuy.restype._
import com.ponkotuy.tool.TempFileTool
import com.ponkotuy.util.Log
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods._
import org.apache.http.client.utils.HttpClientUtils
import org.apache.http.entity.ContentType
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.http.message.BasicNameValuePair
import org.json4s._
import org.json4s.native.Serialization
import org.json4s.native.Serialization.write

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.{Failure, Success, Try}

/** Access To MyFleetGirls
 *
 * @author
 * Date: 14/03/23.
 */
object MFGHttp extends Log {
  val UTF8 = Charset.forName("UTF-8")
  val userAgent = s"${BuildInfo.name} client ver:${BuildInfo.version} w/JVM: ${util.Properties.javaVersion}"
  val config = RequestConfig.custom()
      .setConnectTimeout(60*1000)
      .setRedirectsEnabled(true)
      .build()
  val manager = new PoolingHttpClientConnectionManager(5 * 60, TimeUnit.SECONDS)
  manager.setValidateAfterInactivity(60*1000)
  manager.setDefaultMaxPerRoute(1)
  val httpBuilder = HttpClientBuilder.create()
      .setUserAgent(userAgent)
      .setConnectionManager(manager)
      .setDefaultRequestConfig(config)
      .setRetryHandler(new RetryWithWait(10, 10000L))
  ClientConfig.clientProxyHost.foreach(httpBuilder.setProxy)
  val http = httpBuilder.build()

  implicit val formats = Serialization.formats(NoTypeHints)

  def get(uStr: String, ver: Int = 1): Option[String] = {
    getOrig(ClientConfig.getUrl(2) + uStr)
  }

  def getOrig(url: String): Option[String] = {
    val get = new HttpGet(url)
    executeRequest(get) { res =>
      allRead(res.getEntity.getContent)
    }.recoverWith {
      case e: Throwable =>
        logger.error("GET Reqest fail.",e)
        Failure(e)
    }.toOption
  }

  def post(p: HttpPostable)(implicit auth: Option[Auth], auth2: Option[MyFleetAuth]): Int = {
    p match {
      case m: MasterPostable => masterPost(m.url, m.data, m.ver)
      case n: NormalPostable => post(n.url, n.data, n.ver)
      case f: FilePostable =>
        TempFileTool.save(f.file, f.ext) { file =>
          postFile(f.url, f.fileBodyKey, f.ver)(file)
        }
    }
  }

  def post(uStr: String, data: String, ver: Int = 1)(implicit auth: Option[Auth], auth2: Option[MyFleetAuth]): Int = {
    if(auth.isEmpty) { info(s"Not Authorized: $uStr"); return 1 }
    val url = ClientConfig.postUrl(ver) + uStr
    val content = Map("auth" -> write(auth), "auth2" -> write(auth2), "data" -> data)
    postOrig(url, content)
  }

  def postOrig(url: String, data: Map[String, String]): Int = {
    val post = new HttpPost(url)
    post.setEntity(createEntity(data))
    executeRequest(post) { res =>
      val status = res.getStatusLine.getStatusCode
      if(300 <= status && status < 400) {
        val location = res.getFirstHeader("Location").getValue
        logger.info("Receive redirect statuscode. code:{}, location:{}",status,location)
        postOrig(location, data)
      }
      alertResult(res)
    }.recover {
      case e: Exception =>
        logger.error("POST Request fail.", e)
        1
    }.get
  }

  def masterPost(uStr: String, data: String, ver: Int = 1)(implicit auth2: Option[MyFleetAuth]): Int = {
    val post = new HttpPost(ClientConfig.postUrl(ver) + uStr)
    val entity = createEntity(Map("auth2" -> write(auth2), "data" -> data))
    post.setEntity(entity)
    executeRequest(post) { res =>
      alertResult(res)
    }.recover {
      case e: Exception =>
        logger.error("Master Data POST request",e)
        1
    }.get
  }

  private def executeRequest[A](req: HttpUriRequest)(f: CloseableHttpResponse => A): Try[A] = {
    Try { http.execute(req) }.flatMap { res =>
      try {
        Success(f(res))
      } catch {
        case e: Exception => Failure(e)
      } finally {
        HttpClientUtils.closeQuietly(res)
      }
    }
  }

  private def createEntity(map: Map[String, String]): UrlEncodedFormEntity = {
    val nvps = map.map { case (key, value) =>
      new BasicNameValuePair(key, value)
    }
    new UrlEncodedFormEntity(nvps.asJava, UTF8)
  }

  def postFile(uStr: String, fileBodyKey: String, ver: Int = 1)(file: File)(
      implicit auth: Option[Auth], auth2: Option[MyFleetAuth]): Int = {
    if(auth.isEmpty) { info(s"Not Authorized: $uStr"); return 601 }
    val post = new HttpPost(ClientConfig.postUrl(ver) + uStr)
    val entity = MultipartEntityBuilder.create()
    entity.setCharset(UTF8)
    entity.addTextBody("auth", write(auth), ContentType.APPLICATION_JSON)
    entity.addTextBody("auth2", write(auth2), ContentType.APPLICATION_JSON)
    entity.addPart(fileBodyKey, new FileBody(file))
    post.setEntity(entity.build())
    var res:CloseableHttpResponse = null
    try {
      res = http.execute(post)
      alertResult(res)
    } catch {
      case e: Exception => logger.error("POST file fail.",e); 600
    } finally {
      HttpClientUtils.closeQuietly(res)
    }
  }

  private def alertResult(res: CloseableHttpResponse): Int = {
    val stCode = res.getStatusLine.getStatusCode
    if(stCode >= 400) {
      logger.error("Error Response code:{}, status:{}",stCode,res.getStatusLine.getReasonPhrase)
    }
    stCode
  }

  def existsImage(shipId: Int, kind: String, version: Int): Boolean =
    head(s"/image/ship/${shipId}/${kind}/${version}.png", ver = 2).getStatusLine.getStatusCode == 200

  def existsSound(shipKey: String, soundId: Int, version: Int): Boolean =
    head(s"/sound/ship_obf/${shipKey}/${soundId}/${version}.mp3", ver = 2).getStatusLine.getStatusCode == 200

  def existsMap(area: Int, info: Int, suffix: Int, version: Int): Boolean =
    head(s"/image/map/${area}/${info}/${suffix}/${version}.png", ver = 2).getStatusLine.getStatusCode == 200

  private def head(uStr: String, ver: Int = 1) = {
    val head = new HttpHead(ClientConfig.getUrl(ver) + uStr)
    var res:CloseableHttpResponse = null
    try {
      res = http.execute(head)
    } catch {
      case e: Exception => logger.error("HEAD Request fail.",e)
    } finally {
      HttpClientUtils.closeQuietly(res)
    }
    res
  }

  def allRead(is: InputStream): String = {
    val reader = new InputStreamReader(is, "UTF-8")
    val builder = mutable.StringBuilder.newBuilder
    val buf = new Array[Char](1024)
    var num = reader.read(buf)
    while(0 <= num) {
      builder ++= buf.take(num)
      num = reader.read(buf)
    }
    builder.result()
  }
}

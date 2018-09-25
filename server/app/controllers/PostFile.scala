package controllers

import java.io._
import javax.inject.Inject

import com.ponkotuy.value.ShipIds
import controllers.Common._
import models.db.{ShipImage2nd, MapImage2nd, ShipSound, MasterShipBase}
import play.api.mvc._
import scalikejdbc._

import scala.concurrent.ExecutionContext
import scala.util.Try

/**
 *
 * @author ponkotuy
 * Date: 14/03/22.
 */
class PostFile @Inject()(implicit val ec: ExecutionContext) extends Controller {
  /**
   * 二期HTML5版艦娘画像
   */
  def ship(shipId: Int, kind: String, version: Int) = Action.async(parse.multipartFormData) { request =>
    val form = request.body.asFormUrlEncoded
    authentication(form) { auth =>
      request.body.file("image") match {
        case Some(ref) =>
          if(ShipIds.isEnemy(shipId)) Ok("Unnecessary Enemy")
          else if(ShipImage2nd.find(shipId, kind, version).isDefined) Ok("Already exists")
          else {
            val pngFile = ref.ref.file
            val image = readAll(new FileInputStream(pngFile))
            ShipImage2nd.create(shipId, image, auth.id, kind, version)
            Ok("Success")
          }
        case _ => BadRequest("Need ship image")
      }
    }
  }

  /**
   * 二期HTML5版海域画像
   *
   * 一期はswfファイルにCellPositionも含まれていたが二期では別のjsonなのでCellPositionに当たるものはcontroller.Post.cellPositionへ移動する
   * また、ここで保存する画像はスプライトなので実際に使用する際にはMapFrameを利用して描写すること
   */
  def map(areaId: Int, infoNo: Int, suffix: Int, version: Int) = Action.async(parse.multipartFormData) { request =>
    val form = request.body.asFormUrlEncoded
    authentication(form) { auth =>
      request.body.file("map") match {
        case Some(ref) =>
          if(MapImage2nd.find(areaId, infoNo, suffix, version.toShort).isDefined) Ok("Already exists")
          else {
            val pngFile = ref.ref.file
            val image = readAll(new FileInputStream(pngFile))
            MapImage2nd.create(areaId, infoNo, suffix, image, version.toShort)
            Ok("Success")
          }
        case _ => BadRequest("Need ship image")
      }
    }
  }

  def sound(shipKey: String, soundId: Int, version: Int) = Action.async(parse.multipartFormData) { request =>
    val form = request.body.asFormUrlEncoded
    authentication(form) { auth =>
      request.body.file("sound") match {
        case Some(ref) =>
          findKey(shipKey) { ship =>
            val mp3File = ref.ref.file
            val sound = readAll(new FileInputStream(mp3File))
            try {
              ShipSound.create(ship.id, soundId, version, sound)
              Ok("Success")
            } catch {
              case e: Exception => Ok("Already exists")
            }
          }
        case _ => BadRequest("Need sound")
      }
    }
  }

  private def findKey(key: String)(f: MasterShipBase => Result) = {
    MasterShipBase.findByFilename(key) match {
      case Some(ship) => f(ship)
      case None => Ok("Is enemy, wrong filename or Not found master data")
    }
  }

  private def readAll(is: InputStream): Array[Byte] = {
    val bout = new ByteArrayOutputStream()
    val buffer = new Array[Byte](1024)
    var len = is.read(buffer)
    while(len >= 0) {
      bout.write(buffer, 0, len)
      len = is.read(buffer)
    }
    bout.toByteArray
  }
}

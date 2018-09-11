package controllers

import java.io._
import javax.inject.Inject

import com.ponkotuy.value.ShipIds
import controllers.Common._
import models.db
import models.db.{CellPosition, MapImage}
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
          else if(db.ShipImage2nd.find(shipId, kind, version).isDefined) Ok("Already exists")
          else {
            val pngFile = ref.ref.file
            val image = readAll(new FileInputStream(pngFile))
            db.ShipImage2nd.create(shipId, image, auth.id, kind, version)
            Ok("Success")
          }
        case _ => BadRequest("Need ship image")
      }
    }
  }

  /**
   * 一期Flash版海域画像 使われないのでコメントアウト
   * TODO:二期対応
  def map(areaId: Int, infoNo: Int, version: Int) = Action.async(parse.multipartFormData) { request =>
    val form = request.body.asFormUrlEncoded
    authentication(form) { auth =>
      request.body.file("map") match {
        case Some(ref) =>
          if(db.MapImage.find(areaId, infoNo, version.toShort).isDefined) Ok("Already exists")
          else {
            val swfFile = ref.ref.file
            MapData.fromFile(swfFile) match {
              case Some(mapData) =>
                MapImage.create(areaId, infoNo, mapData.bytes, version.toShort)
                val cp = CellPosition.cp
                if(CellPosition.countBy(sqls.eq(cp.areaId, areaId).and.eq(cp.infoNo, infoNo)) == 0) {
                  mapData.cells.map { cell =>
                    CellPosition.create(areaId, infoNo, cell.cell, cell.posX, cell.posY)
                  }
                }
                Ok("Success")
              case None => BadRequest("SWF parse error")
            }
          }
        case None => BadRequest("Need swf file")
      }
    }
  }*/

  def sound(shipKey: String, soundId: Int, version: Int) = Action.async(parse.multipartFormData) { request =>
    val form = request.body.asFormUrlEncoded
    authentication(form) { auth =>
      request.body.file("sound") match {
        case Some(ref) =>
          findKey(shipKey) { ship =>
            val mp3File = ref.ref.file
            val sound = readAll(new FileInputStream(mp3File))
            try {
              db.ShipSound.create(ship.id, soundId, version, sound)
              Ok("Success")
            } catch {
              case e: Exception => Ok("Already exists")
            }
          }
        case _ => BadRequest("Need sound")
      }
    }
  }

  private def findKey(key: String)(f: db.MasterShipBase => Result) = {
    db.MasterShipBase.findByFilename(key) match {
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

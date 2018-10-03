package controllers

import javax.inject.Inject

import com.ponkotuy.value.{ShipIds, ShipImageKinds}
import models.db
import play.api.mvc._
import scalikejdbc._

import scala.concurrent.ExecutionContext

/**
 *
 * @author ponkotuy
 * Date: 14/03/22.
 */
class RestImage @Inject()(val controllerComponents: ControllerComponents, implicit val ec: ExecutionContext) extends BaseController {
  import controllers.Common._

  def ship2nd(shipId: Int, _kind: String) = actionAsync {
    val kind = if(ShipIds.isEnemy(shipId)) "banner" else _kind
    db.ShipImage2nd.find(shipId, kind) match {
      case Some(img) => Ok(img.image).as("image/png")
      case None =>
        val swfId = ShipImageKinds.toSwfId(kind)
        db.ShipImage.find(shipId, swfId) match {
          case None => NotFound(s"Not Found Image (id=$shipId, kind=$kind)")
          case Some(si) => Redirect(routes.RestImage.ship(si.id, si.swfId))
        }
    }
  }

  def ship2ndHead(shipId: Int, kind: String, version: Int) = actionAsync {
    db.ShipImage2nd.find(shipId, kind, version) match {
      case None => NotFound(s"Not Found Image (id=$shipId)")
      case Some(img) => Ok(img.image).as("image/png")
    }
  }

  def map2nd(areaId: Int, infoNo: Int, suffix: Int) = actionAsync {
    val mi = db.MapImage2nd.mi
    db.MapImage2nd.find(areaId, infoNo, suffix) match {
      case None => NotFound(s"Not found map image (${areaId}-${infoNo}) suffix=${suffix}")
      case Some(img) => Ok(img.image).as("image/png")
    }
  }

  def map2ndHead(areaId: Int, infoNo: Int, suffix: Int, version: Int) = actionAsync {
    db.MapImage2nd.find(areaId, infoNo, suffix, version.toShort) match {
      case None => NotFound(s"Not found map image (${areaId}-${infoNo} suffix=${suffix} ver=${version})")
      case Some(img) => Ok(img.image).as("image/png")
    }
  }

  def ship = shipCommon(_: Int, _: Int)

  def shipHead = shipCommon(_: Int, _: Int)

  def map(areaId: Int, infoNo: Int) = actionAsync {
    val mi = db.MapImage.mi
    db.MapImage.findAllBy(sqls.eq(mi.areaId, areaId).and.eq(mi.infoNo, infoNo)).sortBy(-_.version).headOption match {
      case None => NotFound(s"Not found map image (${areaId}-${infoNo})")
      case Some(img) => Ok(img.image).as("image/jpeg")
    }
  }

  def mapHead(areaId: Int, infoNo: Int, version: Int) = actionAsync {
    db.MapImage.find(areaId, infoNo, version.toShort) match {
      case None => NotFound(s"Not found map image (${areaId}-${infoNo} ver=${version})")
      case Some(img) => Ok(img.image).as("image/jpeg")
    }
  }

  // swfId 5 => 通常画像 7 => 中破画像 1 => 通常画像(small) 3 => 中破画像(small)
  private def shipCommon(shipId: Int, _swfId: Int) = actionAsync {
    val swfId = if(ShipIds.isEnemy(shipId)) 1 else _swfId
    db.ShipImage.find(shipId, swfId) match {
      case Some(record) => Ok(record.image).as("image/jpeg")
      case _ =>
        db.ShipImage.findAllBy(sqls"si.id = ${shipId}").headOption.map { head =>
          Ok(head.image).as("image/jpeg")
        }.getOrElse(NotFound(s"Not Found Image (id=$shipId, swfId=$swfId)"))
    }
  }

  def shipKeyHead(shipKey: String, version: Int) = actionAsync {
    val si = db.ShipImage.si
    db.ShipImage.findAllBy(sqls.eq(si.filename, shipKey).and.eq(si.version, version)).headOption.map { record =>
      Ok(record.image).as("image/jpeg")
    }.getOrElse(NotFound(s"Not Found Image (key=$shipKey)"))
  }
}

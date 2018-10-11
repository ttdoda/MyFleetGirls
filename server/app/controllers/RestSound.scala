package controllers

import javax.inject.Inject

import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import models.db

/**
 *
 * @author ponkotuy
 * Date: 14/03/22.
 */
class RestSound @Inject()(val controllerComponents: ControllerComponents, implicit val ec: ExecutionContext) extends BaseController {
  import controllers.Common._

  def ship(shipId: Int, soundId: Int) = Action.async {
    Future {
      db.ShipSound.findRandomBy(shipId, soundId) match {
        case Some(record) => Ok(record.sound).as("audio/mp3")
        case _ => NotFound(s"Not found sound (shipId=$shipId, soundId=$soundId)")
      }
    }
  }

  def shipHead(shipId: Int, soundId: Int) = Action.async {
    Future {
      db.ShipSound.findRandomBy(shipId, soundId) match {
        case Some(record) => Ok(record.sound).as("audio/mp3")
        case _ => NotFound(s"Not found sound (shipId=$shipId, soundId=$soundId)")
      }
    }
  }

  def shipKeyHead(shipKey: String, soundId: Int, version: Int) = Action.async {
    Future {
      db.ShipSound.findKey(shipKey, soundId, version) match {
        case Some(record) => Ok(record.sound).as("audio/mp3")
        case _ => NotFound(s"Not found sound (shipKey=$shipKey, soundId=$soundId, version=$version)")
      }
    }
  }

  def random() = actionAsync {
    Ok(db.ShipSound.findRandom().sound).as("audio/mp3")
  }
}

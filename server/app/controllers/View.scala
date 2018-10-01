package controllers

import javax.inject.Inject

import build.BuildInfo
import models.db
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

/**
 *
 * @author ponkotuy
 * Date 14/02/24
 */
class View @Inject()(val controllerComponents: ControllerComponents, implicit val ec: ExecutionContext) extends BaseController {
  import controllers.Common._

  def index = Action.async {
    Future {
      val newest = db.Admiral.findNewestWithLv(limit = 20)
      val activeIds = db.Ship.findAllActiveUser(limit = 20)
      val actives = db.Admiral.findAllInWithLv(activeIds)
      val baseCounts = db.UserSettings.countAllByBase()
      Ok(views.html.index(BuildInfo.version, newest, actives, baseCounts))
    }
  }

  def login(init: String, back: String) = actionAsync { request =>
    request.session.get("key").map { key =>
      Redirect(back).withNewSession
    }.getOrElse {
      val baseCounts = db.UserSettings.countAllByBase()
      Ok(views.html.login(init, back, baseCounts))
    }
  }

  def modalMap(areaId: Int, infoNo: Int, cell: Int) = actionAsync(Ok(views.html.modal_map(areaId, infoNo, cell)))
  def modalMapLine(areaId: Int, infoNo: Int, dep: Int, dest: Int) = actionAsync(Ok(views.html.modal_map_line(areaId, infoNo, dep, dest)))

  def modalMap1st(areaId: Int, infoNo: Int, cell: Int) = actionAsync(Ok(views.html.modal_map_1st(areaId, infoNo, cell)))
  def modalMapLine1st(areaId: Int, infoNo: Int, dep: Int, dest: Int) = actionAsync(Ok(views.html.modal_map_line_1st(areaId, infoNo, dep, dest)))

  def admin() = authPonkotu(_ => Ok(views.html.admin.index()))
}

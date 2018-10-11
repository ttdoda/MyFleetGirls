package controllers

import com.google.inject.Inject

import play.api.mvc.{Action, BaseController, ControllerComponents}
import play.api.routing.JavaScriptReverseRouter
/**
 *
 * @author kPherox
 * Date: 18/09/16.
 */
class Application @Inject()(val controllerComponents: ControllerComponents) extends BaseController {
  def javascriptRoutes(page: String) = Action { implicit request =>
    val jsRoutes = page match {
        case "route" =>
          JavaScriptReverseRouter("jsRoutes")(
            routes.javascript.Rest.route,
            routes.javascript.Rest.cellInfo,
            routes.javascript.ViewSta.routeFleet1st,
            routes.javascript.ViewSta.routeFleet2nd
          )
        case "ship_image_book" =>
          JavaScriptReverseRouter("jsRoutes")(
            routes.javascript.RestImage.ship2nd,
            routes.javascript.RestUser.bookShips
          )
        case "naval_battle" =>
          JavaScriptReverseRouter("jsRoutes")(
            routes.javascript.View.modalMap1st,
            routes.javascript.View.modalMapLine1st,
            routes.javascript.View.modalMap,
            routes.javascript.View.modalMapLine,
            routes.javascript.RestUser.battleResultCount,
            routes.javascript.RestUser.battleResult,
            routes.javascript.RestUser.routeLogCount,
            routes.javascript.RestUser.routeLog
          )
        case _ =>
          JavaScriptReverseRouter("jsRoutes")()
      }
    Ok(jsRoutes).as("text/javascript")
  }
}

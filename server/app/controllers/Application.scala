package controllers

import javax.inject.Inject

import play.api.mvc._
import play.api.routing._

/**
 *
 * @author kPherox
 * Date: 18/09/16.
 */
class Application @Inject()() extends Controller {
  def javascriptRoutes = Action { implicit request =>
    Ok(
      JavaScriptReverseRouter("jsRoutes")(
        routes.javascript.RestImage.ship2nd,
        routes.javascript.RestUser.bookShips
      )
    ).as("text/javascript")
  }
}

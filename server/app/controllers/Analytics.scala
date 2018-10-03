package controllers

import com.google.inject.Inject
import play.api.mvc.{Action, BaseController, ControllerComponents}
import tool.Settings

class Analytics @Inject()(val controllerComponents: ControllerComponents) extends BaseController {
  def getId = Action {
    Settings.googleAnalyticsId.fold(NotImplemented("GoogleAnalyticsのIDが設定ファイルに設定されていない"))(Ok(_))
  }
}

package controllers

import javax.inject.Inject

import play.api.mvc.{Action, BaseController, ControllerComponents}

import scala.concurrent.ExecutionContext

import com.ponkotuy.value.KCServer

/**
 * @author ponkotuy
 * Date: 15/03/10.
 */
class MyAssets @Inject()(val controllerComponents: ControllerComponents, implicit val ec: ExecutionContext) extends BaseController {
  val pacDefaultPort = 8080

  def pacDynamicScript(port: Int = pacDefaultPort) = Action {
    Ok(views.html.proxy.render(KCServer.values,port)).as("application/x-ns-proxy-autoconfig")
  }
}

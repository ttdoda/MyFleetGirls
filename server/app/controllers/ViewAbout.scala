package controllers

import javax.inject.Inject

import play.api.mvc.{BaseController, ControllerComponents}
import views.About

import scala.concurrent.ExecutionContext

/**
 *
 * @author ponkotuy
 * Date: 14/10/11.
 \*/
class ViewAbout @Inject()(val controllerComponents: ControllerComponents, implicit val ec: ExecutionContext) extends BaseController {
  import controllers.Common._

  def setup = actionAsync { Redirect(About.Top) }
  def changeLog = actionAsync { Redirect(About.ChangeLog) }
  def faq = actionAsync { Redirect(About.Faq) }
  def setupDetail = actionAsync { Redirect(About.SetupDetail) }
}

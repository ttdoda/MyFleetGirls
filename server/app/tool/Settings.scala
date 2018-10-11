package tool

import com.typesafe.config.ConfigFactory
import play.api.Configuration

/**
 *
 * @author ponkotuy
 * Date: 14/12/08.
 */
object Settings {
  val config = Configuration(ConfigFactory.load())
  val atomCommonKey = config.getOptional[String]("atom.common_key").getOrElse("")
  val googleAnalyticsId = config.getOptional[String]("analytics.google.id")
}

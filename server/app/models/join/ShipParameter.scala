package models.join

import models.db._
import tool.{AntiAirCutin, ShipExperience}
import scala.math.floor

/**
 * Date: 14/06/16.
 */
trait ShipParameter extends GraphData with AntiAirCutin {
  import models.join.ShipParameter._

  def ship: Ship
  def master: MasterShipBase
  def stype: MasterStype
  def spec: MasterShipSpecs

  def id = ship.id
  def shipId = ship.shipId
  def memberId = ship.memberId
  def lv = ship.lv
  def exp = ship.exp
  def nowhp = ship.nowhp
  def maxhp = ship.maxhp
  def fuel = ship.fuel
  def bull = ship.bull
  def dockTime = ship.dockTime
  def cond = ship.cond
  def karyoku = ship.karyoku
  def raisou = ship.raisou
  def taiku = ship.taiku
  def soukou = ship.soukou
  def kaihi = ship.kaihi
  def taisen = ship.taisen
  def sakuteki = ship.sakuteki
  def lucky = ship.lucky
  def kyouka = ship.kyouka
  def locked = ship.locked
  def created = ship.created
  def name = master.name
  def yomi = master.yomi
  def stName = stype.name
  def stAbbName = stAbbNames(stName)

  def isDamaged = nowhp <= (maxhp / 2)
  def damage: Option[Damage] = Damage.fromHp(nowhp, maxhp)

  def slotMaster: Seq[MasterSlotItem]
  def slotNames: Seq[String]

  def hpRate: Double = nowhp / maxhp.toDouble

  /** 素の火力と上昇値 */
  def rawKaryoku: Int = if(kyouka isDefinedAt 0) spec.karyokuMin + kyouka(0) else karyoku - slotMaster.map(_.power).sum
  def upKaryoku: Int = if(kyouka isDefinedAt 0) kyouka(0) else rawKaryoku - spec.karyokuMin
  /** 素の雷装と上昇値 */
  def rawRaisou: Int = if(kyouka isDefinedAt 1) spec.raisouMin + kyouka(1) else raisou - slotMaster.map(_.torpedo).sum
  def upRaisou: Int = if(kyouka isDefinedAt 1) kyouka(1) else rawRaisou - spec.raisouMin
  /** 素の対空と上昇値 */
  def rawTaiku: Int = if(kyouka isDefinedAt 2) spec.taikuMin + kyouka(2) else taiku - slotMaster.map(_.antiair).sum
  def upTaiku: Int = if(kyouka isDefinedAt 2) kyouka(2) else rawTaiku - spec.taikuMin
  /** 素の装甲と上昇値 */
  def rawSoukou: Int = if(kyouka isDefinedAt 3) spec.soukoMin + kyouka(3) else soukou
  def upSoukou: Int = if(kyouka isDefinedAt 3) kyouka(3) else rawSoukou - spec.soukoMin
  /** 運の上昇値 */
  def upLucky: Int = if(kyouka isDefinedAt 4) kyouka(4) else lucky - spec.luckyMin
  /** 耐久の上昇値 */
  def upTaikyu: Int = maxhp - spec.hp
  /** 耐久のカッコカリによる上昇値 */
  def upTaikyuByKakkokari: Int = {
      if(lv <= 99) 0
      else {
        val kakkokari: Int = kakkokariTaikyu(spec.hp)
        if(upTaikyu <= kakkokari) upTaikyu else kakkokari
      }
  }
  /** 耐久の改修による上昇値 */
  def upTaikyuByRemodel: Int = if(kyouka isDefinedAt 5) kyouka(5) else upTaikyu - upTaikyuByKakkokari
  /** 耐久の上昇上限値 */
  def upTaikyuLimit: Int = {
    val hpLimit: Int = spec.hpMax - spec.hp
    val limit: Int = if(lv <= 99) hpLimit else hpLimit - upTaikyuByKakkokari
    if(limit >= 2) 2 else limit
  }
  /** 素の対潜と上昇値 */
  def upTaisen: Int = if(kyouka isDefinedAt 6) kyouka(6) else 0
  def rawTaisen: Int = taisen - slotMaster.map(_.antisub).sum - upTaisen

  /** 改修度 */
  def calcRate(up: Double, upLimit: Double) = if(up >= upLimit) 1.0 else up/upLimit
  def karyokuRate: Double = calcRate(upKaryoku, spec.karyokuMax - spec.karyokuMin)
  def raisouRate: Double = calcRate(upRaisou, spec.raisouMax - spec.raisouMin)
  def taikuRate: Double = calcRate(upTaiku, spec.taikuMax - spec.taikuMin)
  def soukouRate: Double = calcRate(upSoukou, spec.soukoMax - spec.soukoMin)
  def luckyRate: Double = calcRate(upLucky, spec.luckyMax - spec.luckyMin)
  def taisenRate: Double = calcRate(upTaisen, 9)

  /** Condition値による色の変化 */
  def rgb: RGB = ShipParameter.rgb(cond)
  def condBarRGB: RGB = ShipParameter.condBarRGB(cond)

  /** HPによる色の変化 */
  def hpRGB: RGB = ShipParameter.hpRGB(hpRate)

  /** 次のLvまでに必要な経験値の取得率 */
  def expRate: Double = (exp - ShipExperience.sum(lv)).toDouble/ShipExperience.diff(lv + 1)
  /** LvMAX(100 or 175)までに必要な経験値の取得率 */
  def entireExpRate: Double =
    if(lv > 99) exp.toDouble/ShipExperience.sum(175) else exp.toDouble/ShipExperience.sum(100)
}

object ShipParameter {
  implicit class Rate(d: Double) {
    def percentage: Double = floor(d * 10000) / 100
  }

  val stAbbNames = Map(
    "重雷装巡洋艦" -> "雷巡", "重巡洋艦" -> "重巡", "軽巡洋艦" -> "軽巡",
    "航空巡洋艦" -> "航巡", "航空戦艦" -> "航戦",
    "水上機母艦" -> "水母"
  ).withDefault(identity)

  case class RGB(r: Int, g: Int, b: Int) {
    def blend(other: RGB, rate: Double): RGB = {
      def f(x: Int, y: Int, yrate: Double): Int = (x * (1.0 - yrate) + y * yrate).toInt
      RGB(f(r, other.r, rate),
        f(g, other.g, rate),
        f(b, other.b, rate))
    }

    override def toString = f"#$r%2X$g%2X$b%2X"
  }

  val Red = RGB(242, 222, 222)
  val Blue = RGB(217, 237, 247)
  val White = RGB(231, 227, 223)
  val Yellow = RGB(252, 251, 227)

  def rgb(cond: Int): RGB = {
    if(cond > 49) Blue
    else White.blend(Red, (49.0 - cond) / 49.0)
  }

  def condBarRGB(cond: Int): RGB = {
    if(cond > 49) Blue
    else Yellow.blend(Red, (49.0 - cond) / 49.0)
  }

  def hpRGB(rate: Double): RGB = {
    if(rate > 0.5) Yellow.blend(Blue, (rate - 0.5) * 2.0)
    else Red.blend(Yellow, rate * 2.0)
  }

  def kakkokariTaikyu(min: Int): Int = {
    if(min <= 7) 3
    else if(min <= 29) 4
    else if(min <= 39) 5
    else if(min <= 49) 6
    else if(min <= 69) 7
    else if(min <= 90) 8
    else 9
  }
}

sealed abstract class Damage(val name: String)

object Damage {
  case object Minor extends Damage("minor")
  case object Half extends Damage("half")
  case object Major extends Damage("major")

  def fromHp(now: Int, max: Int): Option[Damage] =
    if(now <= max / 4) Some(Major)
    else if(now <= max / 2) Some(Half)
    else if(now <= max * 3 / 4) Some(Minor)
    else None
}

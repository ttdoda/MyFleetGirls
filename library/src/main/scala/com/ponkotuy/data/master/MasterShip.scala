package com.ponkotuy.data.master

import com.ponkotuy.value.ShipIds
import org.json4s._

/**
 *
 * @author ponkotuy
 * Date: 14/02/25
 */
case class MasterShip(base: MasterShipBase, specs: MasterShipSpecs, after: MasterShipAfter, other: MasterShipOther)

object MasterShip {
  implicit val formats = DefaultFormats

  def fromJson(json: JValue, filenames: Int => String): List[MasterShip] = {
    val JArray(xs) = json
    xs.flatMap { x =>
      for {
        base <- MasterShipBase.fromJson(x, filenames)
      } yield {
        val specs = MasterShipSpecs.fromJson(x)
        val after = MasterShipAfter.fromJson(x)
        val other = MasterShipOther.fromJson(x)
        MasterShip(base, specs, after, other)
      }
    }
  }

  def toInt(j: JValue): Int = j.extract[Int]

  def toIntFromJString(j: JValue) = {
    val JString(x) = j
    x.toInt
  }

  def toDoubleInt(j: JValue): (Int, Int) = {
    val List(x, y) = toListInt(j)
    (x, y)
  }

  def toListInt(j: JValue): List[Int] = {
    val JArray(xs) = j
    xs.map(toInt)
  }
}

case class MasterShipBase(
    id: Int, sortno: Int, name: String, yomi: String, stype: Int, filename: String
)

object MasterShipBase {
  import MasterShip._
  def fromJson(x: JValue, filenames: Int => String): Option[MasterShipBase] = {
    val id = toInt(x \ "api_id")
    if(ShipIds.isEnemy(id)) None
    else {
      val sortno = toInt(x \ "api_sortno")
      val JString(name) = x \ "api_name"
      val JString(yomi) = x \ "api_yomi"
      val stype = toInt(x \ "api_stype")
      Some(MasterShipBase(id, sortno, name, yomi, stype, filenames(id)))
    }
  }
}

/**
 *
 * @param soku : 謎のパラメータ
 * @param length : 射程
 */
case class MasterShipSpecs(
    id: Int, hp: Int, hpMax: Int, soukoMin: Int, soukoMax: Int, karyokuMin: Int, karyokuMax: Int,
    raisouMin: Int, raisouMax: Int, taikuMin: Int, taikuMax: Int, luckyMin: Int, luckyMax: Int,
    soku: Int, length: Int, maxeq: List[Int])

object MasterShipSpecs {
  import MasterShip._
  def fromJson(x: JValue): MasterShipSpecs = {
    val id = toInt(x \ "api_id")
    val (hp, hpMax) = toDoubleInt(x \ "api_taik")
    val (soukouMin, soukouMax) = toDoubleInt(x \ "api_souk")
    val (karyokuMin, karyokuMax) = toDoubleInt(x \ "api_houg")
    val (raisouMin, raisouMax) = toDoubleInt(x \ "api_raig")
    val (taikuMin, taikuMax) = toDoubleInt(x \ "api_tyku")
    val (luckyMin, luckyMax) = toDoubleInt(x \ "api_luck")
    val soku = toInt(x \ "api_soku")
    val length = toInt(x \ "api_leng")
    val maxeq = toListInt(x \ "api_maxeq")
    MasterShipSpecs(
      id, hp, hpMax, soukouMin, soukouMax, karyokuMin, karyokuMax, raisouMin, raisouMax, taikuMin, taikuMax,
      luckyMin, luckyMax, soku, length, maxeq
    )
  }
}

/**
 *
 * @param afterlv : 改造Lv
 * @param afterfuel : 改造に必要な燃料
 */
case class MasterShipAfter(id: Int, afterlv: Int, aftershipid: Int, afterfuel: Int, afterbull: Int)

object MasterShipAfter {
  import MasterShip._
  def fromJson(x: JValue): MasterShipAfter = {
    val id = toInt(x \ "api_id")
    val afterlv = toInt(x \ "api_afterlv")
    val aftershipid = toIntFromJString(x \  "api_aftershipid")
    val afterfuel = toInt(x \ "api_afterfuel")
    val afterbull = toInt(x \ "api_afterbull")
    MasterShipAfter(id, afterlv, aftershipid, afterfuel, afterbull)
  }
}

/**
 *
 * @param buildtime : 建造時間
 * @param backs : 入手時背景画像（Rarity）
 * @param fuelMax : MAX所持燃料
 * @param powupFuel : 燃料ではなく火力
 * @param powupAmmo : 弾薬ではなく雷装
 * @param powupSteel : 鋼鉄ではなく対空
 * @param powupBauxite : ボーキサイトではなく装甲
 */
case class MasterShipOther(
    id: Int, buildtime: Int,
    brokenFuel: Int, brokenAmmo: Int, brokenSteel: Int, brokenBauxite: Int,
    powupFuel: Int, powupAmmo: Int, powupSteel: Int, powupBauxite: Int,
    backs: Int, fuelMax: Int, bullMax: Int, slotNum: Int)

object MasterShipOther {
  import MasterShip._

  def fromJson(x: JValue): MasterShipOther = {
    val id = toInt(x \ "api_id")
    val buildtime = toInt(x \ "api_buildtime")
    val List(brokenFuel, brokenAmmo, brokenSteel, brokenBauxite) = toListInt(x \ "api_broken")
    val List(powupFuel, powupAmmo, powupSteel, powupBauxite) = toListInt(x \ "api_powup")
    val backs = toInt(x \ "api_backs")
    val fuelMax = toInt(x \ "api_fuel_max")
    val bullMax = toInt(x \ "api_bull_max")
    val slotNum = toInt(x \ "api_slot_num")
    MasterShipOther(
      id, buildtime,
      brokenFuel, brokenAmmo, brokenSteel, brokenBauxite, powupFuel, powupAmmo, powupSteel, powupBauxite,
      backs, fuelMax, bullMax, slotNum
    )
  }
}

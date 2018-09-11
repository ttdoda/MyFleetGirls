package com.ponkotuy.value

/**
 *
 * @author kPherox
 * Date: 18/09/11.
 */
object ShipImageKinds {
  trait Kind {
    def name: String
    def swfId: Int = 0
  }

  trait KindWithDmg extends Kind {
    def parent: Kind = this

    trait Dmg extends Kind {
      override def swfId = if(parent.swfId > 0) parent.swfId + 2 else NonDefined.swfId
    }

    object Damage extends Dmg {
      def name = s"${parent.name}_dmg"
    }

    object Gray extends Dmg {
      def name = s"${parent.name}_g_dmg"
    }
  }

  object NonDefined extends Kind {
    def name = "none"
  }

  object Banner extends KindWithDmg {
    def name = "banner"
    override def swfId = 1
  }

  object Card extends KindWithDmg {
    def name = "card"
    override def swfId = 5
  }

  object Full extends KindWithDmg {
    def name = "full"
    override def swfId = 17
  }

  object SupplyCharacter extends KindWithDmg {
    def name = "supply_character"
  }

  val kinds: Vector[Kind] = Vector(
    NonDefined,
    Banner,
    Banner.Damage,
    Banner.Gray,
    Card,
    Card.Damage,
    Full,
    Full.Damage,
    SupplyCharacter,
    SupplyCharacter.Damage
  )

  def toSwfId(name: String): Int = kinds.find(_.name == name) match {
    case Some(kind) => kind.swfId
    case _ => NonDefined.swfId
  }

  def toName(swfId: Int): String = kinds.find(_.swfId == swfId) match {
    case Some(kind) => kind.name
    case _ => NonDefined.name
  }
}

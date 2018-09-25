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

  object CharacterUp extends KindWithDmg {
    def name = "character_up"
    override def swfId = 9
  }

  object CharacterFull extends KindWithDmg {
    def name = "character_full"
    override def swfId = 13
  }

  object Full extends KindWithDmg {
    def name = "full"
    override def swfId = 17
  }

  object Remodel extends KindWithDmg {
    def name = "remodel"
    override def swfId = 21
  }

  object AlbumStatus extends Kind {
    def name = "album_status"
    override def swfId = 25
  }

  object SupplyCharacter extends KindWithDmg {
    def name = "supply_character"
    override def swfId = 27
  }

  val kinds: Vector[Kind] = Vector(
    NonDefined,
    Banner,
    Banner.Damage,
    Banner.Gray,
    Card,
    Card.Damage,
    CharacterUp,
    CharacterUp.Damage,
    CharacterFull,
    CharacterFull.Damage,
    Full,
    Full.Damage,
    Remodel,
    Remodel.Damage,
    AlbumStatus,
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

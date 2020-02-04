package com.ponkotuy.restype

import com.ponkotuy.data
import com.ponkotuy.parser.Query
import org.json4s.native.Serialization.write

import scala.util.matching.Regex

/**
 * @author ponkotuy
 * Date: 15/04/12.
 */
case object CreateItem extends ResType {
  import ResType._

  override def regexp: Regex = s"\\A$ReqKousyou/createitem\\z".r

  override def postables(q: Query): Seq[Result] = {
    FleetsState.firstFleet.flatMap(_.firstShip).map { flag =>
      val createItems = data.CreateItems.from(q.req, q.obj, flag)
      NormalPostable("/createitems", write(createItems), 1, createItems.map(_.summary).mkString("\n"))
    }.toList
  }
}

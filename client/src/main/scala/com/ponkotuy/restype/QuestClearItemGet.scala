package com.ponkotuy.restype

import com.ponkotuy.data
import com.ponkotuy.parser.Query
import org.json4s.native.Serialization.write

import scala.util.matching.Regex

/**
 * @author ponkotuy
 * Date: 15/04/12.
 */
case object QuestClearItemGet extends ResType {
  import ResType._

  override def regexp: Regex = s"\\A$ReqQuest/clearitemget\\z".r

  override def postables(q: Query): Seq[Result] = {
    val qClearItem = data.QuestClearItem.fromJson(q.obj, q.req)
    if (qClearItem.nonEmpty) {
      NormalPostable("/questclearitem", write(qClearItem), 2) :: Nil
    } else Nil
  }
}

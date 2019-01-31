package com.ponkotuy.data

import com.ponkotuy.tool.PostQueryParser
import org.scalatest.FunSuite
import org.json4s.native.JsonMethods._

/**
 *
 * @author ponkotuy
 * Date: 15/02/05.
 */
class QuestClearItemSuite extends FunSuite {
  test("normal pattern") {
    val jsonValue = """{"api_result":1,"api_result_msg":"\u6210\u529f","api_data":{"api_material":[800,800,800,800],"api_bounus_count":2,"api_bounus":[{"api_type":1,"api_count":5,"api_item":{"api_id":8,"api_name":""}},{"api_type":18,"api_count":200}]}}"""
    val postValue = """api%5Fverno=1&api%5Fquest%5Fid=888&api%5Ftoken=xxxx"""
    val value = QuestClearItem.fromJson(parse(jsonValue) \ "api_data", PostQueryParser.parse(postValue))
    val material = QuestMaterial(800,800,800,800)
    val bounus = List(QuestBounus(1, 5, Option(QuestBounusItem(8, ""))), QuestBounus(18, 200, None))
    val expected = QuestClearItem(888, material, 2, bounus)

    assert(value.isDefined)
    assert(value.get === expected)
  }
}

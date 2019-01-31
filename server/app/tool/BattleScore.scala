package tool

import com.github.nscala_time.time.Imports._
import models.db._
import models.join.Stage
import scalikejdbc._

/**
  * Date: 2015/12/23
  * @author ponkotuy
  */
case class BattleScore(monthlyExp: Int, yearlyExp: Int, eo: Int, lastEo: Int, quest: Int) {
  def pp: String = {
    s"${sum}(${toString})"
  }

  def sum: Int = monthlyExp + yearlyExp + eo + lastEo

  def toCalcScore(memberId: Long, yyyymmddhh: Int, created: Long) =
    CalcScore(memberId, monthlyExp, yearlyExp, eo, lastEo, quest, yyyymmddhh, created)
}

object BattleScore {
  import util.MFGDateUtil._
  case class FromExp(monthly: Int, yearly: Int)

  object FromExp {
    def empty = FromExp(0, 0)
  }

  def calcFromMemberId(memberId: Long): BattleScore = {
    val exp = fromExp(memberId)
    val now = DateTime.now(Tokyo)
    val mHead = monthHead(now)
    val nowMonth = new Interval(mHead, now)
    val eo = calcNowEo(memberId, nowMonth)
    val lastEo = if(now.getMonthOfYear == 1) 0 else calcEo(memberId, new Interval(monthHead(now - 1.month), mHead)) / 35
    val quest = calcQuest(memberId, nowMonth)
    BattleScore(exp.monthly, exp.yearly, eo, lastEo, quest)
  }

  private def fromExp(memberId: Long): FromExp = {
    val b = Basic.b
    val now = DateTime.now(Tokyo)
    Basic.findByUser(memberId).map(_.experience).fold(FromExp.empty) { nowExp =>
      val lastMonthExp =
        Basic.findExpBy(sqls.eq(b.memberId, memberId).and.gt(b.created, monthHead(now).getMillis), b.created).getOrElse(nowExp)
      val lastYearExp =
        Basic.findExpBy(sqls.eq(b.memberId, memberId).and.gt(b.created, yearHead(now).getMillis), b.created).getOrElse(lastMonthExp)
      val monthly = (nowExp - lastMonthExp) * 7 / 10000
      val yearly = (lastMonthExp - lastYearExp) / 50000
      FromExp(monthly, yearly)
    }
  }

  private def calcNowEo(memberId: Long, interval: Interval): Int = {
    StageInfo.values.map { info =>
      val isClear = isClearFromMapInfo(memberId, info.stage, interval.end)
          .getOrElse(info.clear <= clearCountFromBattle(memberId, info.stage, interval))
      if(isClear) info.score else 0
    }.sum
  }

  private def calcEo(memberId: Long, interval: Interval): Int = {
    StageInfo.values.map { info =>
      val count = if(info.boss) {
        clearCountFromBattle(memberId, info.stage, interval)
      } else {
        clearCountFromRoute(memberId, info.stage, interval)
      }
      if (info.clear <= count) info.score else 0
    }.sum
  }

  private def calcQuest(memberId: Long, interval: Interval): Int = {
    val qci = QuestClearItem.qci
    QuestClearItem.findAllByUserAndBounusType(memberId, 18, Option(intervalToSQLSyntax(qci.created, interval))).map(_.count).sum
  }

  /**
    * mapInfoの情報が古いときはNoneを返す
    */
  private def isClearFromMapInfo(memberId: Long, stage: Stage, from: DateTime): Option[Boolean] =
    MapInfo.findStage(stage, memberId).find(monthHead(from).getMillis <= _.created).map(_.cleared)

  private def clearCountFromBattle(memberId: Long, stage: Stage, interval: Interval): Long = {
    val br = BattleResult.br
    val bosses = findBossCells(stage)
    BattleResult.countBy(
      sqls.eq(br.memberId, memberId)
          .and.eq(br.areaId, stage.area)
          .and.eq(br.infoNo, stage.info)
          .and.in(br.cell, bosses)
          .and.in(br.winRank, Seq("S", "A", "B")) // 厳密には勝利しただけでカウントは進まないが、終わったのに終わってないと判断される方が怖いのでSABまで入れる
          .and(intervalToSQLSyntax(br.created, interval))
    )
  }

  private def clearCountFromRoute(memberId: Long, stage: Stage, interval: Interval): Long = {
    val mr = MapRoute.mr
    val bosses = findBossCells(stage)
    MapRoute.countBy(
      sqls.eq(mr.memberId, memberId)
          .and.eq(mr.areaId, stage.area)
          .and.eq(mr.infoNo, stage.info)
          .and.in(mr.dest, bosses)
          .and(intervalToSQLSyntax(mr.created, interval))
    )
  }

  private def findBossCells(stage: Stage): Seq[Int] = {
    val ci = CellInfo.ci
    val cell = CellInfo.findAllBy(sqls.eq(ci.areaId, stage.area).and.eq(ci.infoNo, stage.info))
    cell.filter(_.boss).map(_.cell)
  }

  /**
    * BIGINTカラムの範囲指定を生成
    */
  private def intervalToSQLSyntax(column: SQLSyntax, interval: Interval): SQLSyntax =
    sqls.gt(column, interval.startMillis).and.lt(column, interval.endMillis)
}

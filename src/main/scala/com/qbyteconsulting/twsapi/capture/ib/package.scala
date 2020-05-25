package com.qbyteconsulting.twsapi.capture

import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant, LocalDateTime, ZoneId}

import com.ib.client.Types.BarSize
import com.ib.client.{Contract, ContractDetails}
import com.qbyteconsulting.reactor.ReactorEvent
import com.qbyteconsulting.twsapi.capture.config.ContractConfig
import com.qbyteconsulting.twsapi.capture.ib.schedule.TradingSession

package object ib {

  case class IbHostParams(host: String = "127.0.0.1",
                          port: Int = 7496,
                          clientId: Int = 0)

  case class ConnectionSuccess() extends ReactorEvent

  case class ConnectionClosed() extends ReactorEvent

  case class ConnectionFail(t: Throwable) extends ReactorEvent

  case class TwsClock(val clock: Clock) extends ReactorEvent

  case class Reconnect() extends ReactorEvent

  case class Reload() extends ReactorEvent

  case class ConnectionCheck(val startTime: Instant) extends ReactorEvent

  case class RequestError(requestId: Int, errorCode: Int, errorMsg: String)
    extends ReactorEvent

  case class Status(code: Int, msg: String) extends ReactorEvent

  case class Status502(val message: String = "Couldn't connect to TWS.")
    extends ReactorEvent

  case class Status504(val message: String = "Not connected to TWS.")
    extends ReactorEvent

  case class Status507(val message: String = "Bad Message Length")
    extends ReactorEvent

  case class Status1100(val message: String =
                        "Connectivity between IB and the TWS has been lost.")
    extends ReactorEvent

  case class Status1101(val message: String =
                        "Connectivity between IB and TWS has been restored- data lost.")
    extends ReactorEvent

  case class Status1102(val message: String =
                        "Connectivity between IB and TWS has been restored- data maintained.")
    extends ReactorEvent

  case class Status2103(val farm: String,
                        val message: String =
                        "A market data farm is disconnected.")
    extends ReactorEvent

  case class Status2104(val message: String = "Market data farm connection is OK")
    extends ReactorEvent

  case class ContractsConfigured(val contracts: Iterable[ContractConfig])
    extends ReactorEvent

  case class ContractDetailsLoaded(val contractDetails: ContractDetails)
    extends ReactorEvent

  case class RequestMarketData(tradingSession: TradingSession)
    extends ReactorEvent

  case class CancelMarketData(tradingSession: TradingSession)
    extends ReactorEvent

  case class TwsZoneId(zoneId: ZoneId) extends ReactorEvent

  case class RequestHistoricalData(tickerId: Int,
                                   contract: Contract,
                                   endDateTime: String,
                                   durationStr: String,
                                   barSizeSetting: String,
                                   whatToShow: String,
                                   useRTH: Int,
                                   formatDate: Int,
                                   keepUpToDate: Boolean)
    extends ReactorEvent

  case class FinishHistoricalData(tickerId: Int, timestamp: Long, fromTime: Long, toTime: Long)
    extends ReactorEvent

  val DayFmt = "yyyyMMdd"

  val SecFmt = "yyyyMMdd HHmmss"

  val HeadFmt = "yyyyMMdd  HH:mm:ss"

  private val DAY_FMT = DateTimeFormatter.ofPattern(DayFmt)

  private val SEC_FMT = DateTimeFormatter.ofPattern(SecFmt)

  private val HEAD_FMT = DateTimeFormatter.ofPattern(HeadFmt)

  /* 60 S	1 sec - 1 mins
  120 S	1 sec - 2 mins
  1800 S (30 mins)	1 sec - 30 mins
  3600 S (1 hr)	5 secs - 1 hr
  14400 S (4hr)	10 secs - 3 hrs
  28800 S (8 hrs)	30 secs - 8 hrs
  1 D	1 min - 1 day
  2 D	2 mins - 1 day
  1 W	3 mins - 1 week
  1 M	30 mins - 1 month
  1 Y	1 day - 1 month

      _1_secs("1 secs"),
  _5_secs("5 secs"),
  _10_secs("10 secs"),
  _15_secs("15 secs"),
  _30_secs("30 secs"),
  _1_min("1 min"),
  _2_mins("2 mins"),
  _3_mins("3 mins"),
  _5_mins("5 mins"),
  _10_mins("10 mins"),
  _15_mins("15 mins"),
  _20_mins("20 mins"),
  _30_mins("30 mins"),
  _1_hour("1 hour"),
  _4_hours("4 hours"),
  _1_day("1 day"),
  _1_week("1 week"),
  _1_month("1 month");
  */

  case class StepSize(amount: Int, unit: ChronoUnit) {
    def timeunit = unit match {
      case ChronoUnit.SECONDS => "S"
      case ChronoUnit.DAYS => "D"
      case ChronoUnit.YEARS => "Y"
    }
    override def toString: String = s"$amount $timeunit"
  }

  def biggestStepSize(barSize: BarSize): StepSize = barSize match {
    case BarSize._1_secs => StepSize(1800, ChronoUnit.SECONDS) // 30M
    case BarSize._5_secs => StepSize(3600, ChronoUnit.SECONDS) // 1H
    case BarSize._10_secs |
         BarSize._15_secs => StepSize(14400, ChronoUnit.SECONDS) // 4H
    case BarSize._30_secs => StepSize(28800, ChronoUnit.SECONDS) // 8H
    case BarSize._1_min => StepSize(1, ChronoUnit.DAYS) // today
    case BarSize._2_mins => StepSize(2, ChronoUnit.DAYS) // today + yesterday
    case BarSize._3_mins |
         BarSize._5_mins |
         BarSize._10_mins |
         BarSize._15_mins |
         BarSize._20_mins => StepSize(5, ChronoUnit.DAYS) // 1W
    case BarSize._30_mins |
         BarSize._1_hour |
         BarSize._4_hours => StepSize(22, ChronoUnit.DAYS) // 1M
    case BarSize._1_day => StepSize(1, ChronoUnit.YEARS) // 1Y
  }

  def toTimeMillis(date: String, zone: ZoneId): Long =
    if (date.length == DayFmt.length) toMillis(date, DAY_FMT.withZone(zone))
    else if (date.length == SecFmt.length) toMillis(date, SEC_FMT.withZone(zone))
    else if (date.length == HeadFmt.length) toMillis(date, HEAD_FMT.withZone(zone))
    else
      throw new IllegalArgumentException(
        s"date $date is not in a valid date format")

  private def toMillis(date: String, fmt: DateTimeFormatter) = LocalDateTime.parse(date, fmt)
    .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

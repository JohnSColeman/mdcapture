package com.qbyteconsulting.twsapi.capture.ib

import java.text.SimpleDateFormat
import java.util.TimeZone

import com.ib.client.{Contract, ContractDetails}
import com.qbyteconsulting.twsapi.capture.reactor
import com.qbyteconsulting.twsapi.capture.reactor.{Reactor, ReactorCore}

object StateReactor {

  private val DateTimePattern = "yyyyMMdd:HHmm"

  case class TradingPeriod(contract: Contract, open: Long, close: Long)

  def getTradingPeriods(
      contractDetails: ContractDetails): Seq[TradingPeriod] = {
    try {
      val days = contractDetails
        .tradingHours()
        .split(";")
        .filter(!_.endsWith("CLOSED"))
        .toList
      val timeZone = TimeZone.getTimeZone(contractDetails.timeZoneId())
      val dateFmt = new SimpleDateFormat(DateTimePattern)
      dateFmt.setTimeZone(timeZone)
      days.map { range =>
        val (from, to) = range.split("-") match {
          case Array(from, to) => (from, to)
        }
        TradingPeriod(contractDetails.contract(),
                      dateFmt.parse(from).getTime,
                      dateFmt.parse(to).getTime)
      }
    } catch {
      case e: Exception => {
        e.printStackTrace()
        Seq.empty[TradingPeriod]
      }
    }
  }
}

class StateReactor(val reactorCore: ReactorCore)
    extends Reactor
    with StatusListener {
  import StateReactor._

  override def handleEvent(event: reactor.ReactorEvent): Unit = {
    event match {
      case ContractLoaded(contractDetails) => loadSchedule(contractDetails)
      case _                               => ()
    }
  }

  override def connectionSuccess(): Unit = publishEvent(ConnectionSuccess())

  override def error(id: Int, errorCode: Int, errorMsg: String): Unit = {
    val event = errorCode match {
      case 502  => Status502()
      case 504  => Status504()
      case 1100 => Status1100()
      case 1101 => Status1101()
      case 1102 => Status1102()
      case 2103 => {
        val farm =
          errorMsg.substring(errorMsg.indexOf(":") + 1, errorMsg.length)
        Status2103(farm)
      }
      case _ => ErrorStatus(id, errorCode, errorMsg)
    }
    publishEvent(event)
  }

  private def loadSchedule(contractDetails: ContractDetails): Unit = {
    val periods = getTradingPeriods(contractDetails)
    periods.foreach { period =>
      val now = System.currentTimeMillis()
      if (period.open <= now && period.close >= now) {
        publishEvent(RequestMarketData(contractDetails.contract()))
        scheduleEvent(CancelMarketData(contractDetails.conid()),
                      (period.close - now))
      } else if (period.open >= now) {
        scheduleEvent(RequestMarketData(contractDetails.contract()),
                      period.open - now)
        scheduleEvent(CancelMarketData(contractDetails.conid()),
                      (period.close - now))
      }
    }
  }
}

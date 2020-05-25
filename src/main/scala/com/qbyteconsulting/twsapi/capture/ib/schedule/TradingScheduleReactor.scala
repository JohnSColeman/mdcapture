package com.qbyteconsulting.twsapi.capture.ib.schedule

import java.lang.management.ManagementFactory
import java.text.SimpleDateFormat
import java.time.Clock
import java.util.TimeZone

import com.ib.client.ContractDetails
import com.qbyteconsulting.reactor.{Reactor, ReactorCore, _}
import com.qbyteconsulting.twsapi.capture.ib.ContractDb.ConId
import com.qbyteconsulting.twsapi.capture.ib._
import com.qbyteconsulting.twsapi.capture.ib.schedule.TradingSession.{
  RequestTimes,
  SessionStatus
}
import javax.management.ObjectName

import scala.collection.mutable
import scala.util.Try

object TradingScheduleReactor {

  private val HoursDateTimePattern = "yyyyMMdd:HHmm"

  private def createTradingSessions(contractDetails: ContractDetails,
                                    twsClock: Clock): Seq[TradingSession] =
    try {
      val days = contractDetails
        .tradingHours()
        .split(";")
        .filter(_.contains("-"))
        .toList
      val timeZone = TimeZone.getTimeZone(contractDetails.timeZoneId())
      val dateFmt = new SimpleDateFormat(HoursDateTimePattern)
      dateFmt.setTimeZone(timeZone)
      days.map { range =>
        val (from, to) = range.split("-") match { case Array(f, t) => (f, t) }
        new TradingSession(contractDetails.contract(),
                           dateFmt.parse(from).toInstant,
                           dateFmt.parse(to).toInstant,
                           timeZone)(twsClock)
      }
    } catch { // this should not happen
      case e: Exception => {
        e.printStackTrace()
        Seq.empty[TradingSession]
      }
    }

  private class TradingCalendar {

    private val mBeanServer = ManagementFactory.getPlatformMBeanServer()

    private val tradingCalendar = new mutable.ArrayBuffer[TradingSession]()

    private def objectName(session: TradingSession) =
      new ObjectName(
        s"com.qbyteconsulting.twsapi.capture:type=TradingCalendar,id=${session.contract
          .conid()},date=${session.date}")

    def hasSession(session: TradingSession) = tradingCalendar.contains(session)

    def addSession(session: TradingSession) = {
      if (session.sessionStatus != SessionStatus.Closed) {
        tradingCalendar += session
        Try {
          mBeanServer.registerMBean(session, objectName(session))
        }
      }
    }

    def removeSession(session: TradingSession): Unit = {
      tradingCalendar -= session
      Try {
        mBeanServer.unregisterMBean(objectName(session))
      }
    }

    def getRequestTimes(conid: ConId): Seq[Option[RequestTimes]] =
      tradingCalendar
        .filter(session => session.contract.conid() == conid)
        .map(session => session.getRequestTimes)
  }
}

class TradingScheduleReactor(val reactorCore: ReactorCore) extends Reactor {
  import TradingScheduleReactor._

  private val tradingCalendar = new TradingCalendar()
  private var twsClock: Clock = _

  override def onEvent(event: ReactorEvent): Unit =
    event match {
      case TwsClock(clock)                 => twsClock = clock
      case ContractDetailsLoaded(contractDetails) => scheduleEvents(contractDetails)
      case RequestMarketData(session)      => openTradingSession(session)
      case CancelMarketData(session)       => closeTradingSession(session)
      case _                               => Unit
    }

  private def scheduleEvents(contractDetails: ContractDetails): Unit = {
    createTradingSessions(contractDetails, twsClock).foreach { session =>
      if (!tradingCalendar.hasSession(session)) {
        tradingCalendar.addSession(session)
      }
    }
    tradingCalendar.getRequestTimes(contractDetails.conid()).foreach {
      case Some(RequestTimes(session, None, Some(close))) => {
        publish(RequestMarketData(session))
        schedule(CancelMarketData(session), close)
      }
      case Some(RequestTimes(session, None, None)) => {
        publish(RequestMarketData(session))
      }
      case Some(RequestTimes(session, Some(open), Some(close))) => {
        schedule(RequestMarketData(session), open)
        schedule(CancelMarketData(session), close)
      }
      case _ =>
    }
  }

  private def openTradingSession(session: TradingSession) = ()

  private def closeTradingSession(session: TradingSession) =
    tradingCalendar.removeSession(session)

}

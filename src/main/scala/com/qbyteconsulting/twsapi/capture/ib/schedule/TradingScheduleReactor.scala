package com.qbyteconsulting.twsapi.capture.ib.schedule

import java.lang.management.ManagementFactory
import java.text.SimpleDateFormat
import java.util.TimeZone

import com.ib.client.{Contract, ContractDetails}
import com.qbyteconsulting.twsapi.capture.ib.ContractDb.ConId
import com.qbyteconsulting.twsapi.capture.ib._
import com.qbyteconsulting.twsapi.capture.reactor
import com.qbyteconsulting.twsapi.capture.reactor.{Reactor, ReactorCore}
import javax.management.ObjectName

import scala.collection.mutable
import scala.util.Try

object TradingScheduleReactor {

  private val HoursDateTimePattern = "yyyyMMdd:HHmm"

  def getTradingSessions(
      contractDetails: ContractDetails): Seq[TradingSession] =
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
        val (from, to) = range.split("-") match {
          case Array(from, to) => (from, to)
        }
        new TradingSession(contractDetails.contract(),
                           dateFmt.parse(from).toInstant,
                           dateFmt.parse(to).toInstant,
                           timeZone)
      }
    } catch {
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
      if (!session.isClosed) {
        tradingCalendar += session
        Try {
          mBeanServer.registerMBean(session, objectName(session))
        }
      }
    }

    def removeSession(session: TradingSession) = {
      tradingCalendar -= session
      Try {
        mBeanServer.unregisterMBean(objectName(session))
      }
    }

    def getActiveSessions(): Seq[TradingSession] =
      tradingCalendar.filter(_.isActive)
  }

}

class TradingScheduleReactor(val reactorCore: ReactorCore) extends Reactor {
  import TradingScheduleReactor._

  private val scheduledSessions = new mutable.ArrayBuffer[TradingSession]()
  private val tradingCalendar = new TradingCalendar()

  override def onEvent(event: reactor.ReactorEvent): Unit =
    event match {
      case ContractLoaded(contractDetails) => scheduleEvents(contractDetails)
      case RequestMarketData(contract)     => openTradingSession(contract)
      case CancelMarketData(conid: ConId)  => closeTradingSession(conid)
      case _                               => Unit
    }

  private def scheduleEvents(contractDetails: ContractDetails): Unit = {
    getTradingSessions(contractDetails).foreach { session =>
      if (!tradingCalendar.hasSession(session)) {
        tradingCalendar.addSession(session)
      }
      if (session.isActive) {
        if (!scheduledSessions.contains(session)) {
          scheduledSessions += session
          publish(RequestMarketData(contractDetails.contract()))
          schedule(CancelMarketData(contractDetails.conid()), session.close)
        } else {
          publish(RequestMarketData(contractDetails.contract()))
        }
      } else if (session.isPending && !scheduledSessions.contains(session)) {
        scheduledSessions += session
        schedule(RequestMarketData(contractDetails.contract()), session.open)
        schedule(CancelMarketData(contractDetails.conid()), session.close)
      }
    }
  }

  private def openTradingSession(contract: Contract) =
    scheduledSessions
      .filter(s => s.contract.conid() == contract.conid() && s.isActive)
      .foreach { as =>
        // update calendar?
      }

  private def closeTradingSession(conid: ConId) =
    scheduledSessions
      .filter(s => s.contract.conid() == conid && s.isClosed)
      .foreach { cs =>
        scheduledSessions -= cs
        tradingCalendar.removeSession(cs)
      }
}

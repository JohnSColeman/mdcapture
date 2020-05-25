package com.qbyteconsulting.twsapi.capture.ib.schedule

import java.text.SimpleDateFormat
import java.time.{Clock, Instant}
import java.util.{Date, TimeZone}

import com.ib.client.Contract
import com.qbyteconsulting.twsapi.capture.ib.schedule.TradingSession.SessionStatus.SessionStatus

object TradingSession {

  private val ShortDateTimePattern = "yyyy MMMdd HHmm"

  private def getShortDate(instant: Instant, timeZone: TimeZone) = {
    val shortDateFmt = new SimpleDateFormat(ShortDateTimePattern)
    shortDateFmt.setTimeZone(timeZone)
    shortDateFmt.format(new Date(instant.toEpochMilli))
  }

  case class RequestTimes(val session: TradingSession,
                          val open: Option[Instant],
                          val close: Option[Instant])

  object SessionStatus extends Enumeration {
    type SessionStatus = Value
    val Closed, Open, Pending = Value
  }
}

case class TradingSession(val contract: Contract,
                          val open: Instant,
                          val close: Instant,
                          val timeZone: TimeZone)(val twsClock: Clock)
    extends TradingSessionMBean {
  import TradingSession._

  val id =
    s"${contract.conid()} ${getShortDate(open, timeZone)}"

  val date = s"${getShortDate(open, timeZone)}${timeZone.getID}"

  private var scheduled = false

  private var lastActive: Option[Instant] = None

  override def getSession(): String = toString()

  def updateLastActive(): Unit = lastActive = Option(twsClock.instant())

  def getLastActive: Option[Instant] = lastActive

  def marketState: String = sessionStatus.toString

  def sessionStatus: SessionStatus = {
    if (twsClock.instant().isAfter(close)) SessionStatus.Closed
    else if (open.isAfter(twsClock.instant())) SessionStatus.Pending
    else SessionStatus.Open
  }

  def getRequestTimes: Option[RequestTimes] = {
    val status = sessionStatus
    if (status == SessionStatus.Open) {
      if (!scheduled) {
        scheduled = true
        Some(RequestTimes(this, None, Some(close)))
      } else Some(RequestTimes(this, None, None))
    } else if (status == SessionStatus.Pending) {
      if (!scheduled) {
        scheduled = true
        Some(RequestTimes(this, Some(open), Some(close)))
      } else None
    } else None
  }

  def elapsedSinceLastActive = {
    if (sessionStatus == SessionStatus.Open) {
      if (lastActive.isDefined) {
        val elapsed = System.currentTimeMillis() - lastActive.get.toEpochMilli
        val seconds = elapsed / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        "%02dh:%02dm:%02ds".format(hours, minutes, seconds)
      } else "NA"
    } else ""
  }

  override def toString(): String =
    s"${contract.description()} ${new Date(open.toEpochMilli)} to ${new Date(
      close.toEpochMilli)} $marketState $elapsedSinceLastActive"
}

package com.qbyteconsulting.twsapi.capture.ib.schedule

import java.text.SimpleDateFormat
import java.time.Instant
import java.util.{Date, TimeZone}

import com.ib.client.Contract

object TradingSession {

  private val ShortDateTimePattern = "yyyy MMMdd HHmm"

  private def getShortDate(instant: Instant, timeZone: TimeZone) = {
    val shortDateFmt = new SimpleDateFormat(ShortDateTimePattern)
    shortDateFmt.setTimeZone(timeZone)
    shortDateFmt.format(new Date(instant.toEpochMilli))
  }
}

case class TradingSession(val contract: Contract,
                          val open: Instant,
                          val close: Instant,
                          val timeZone: TimeZone)
    extends TradingSessionMBean {
  import TradingSession._

  val id =
    s"${contract.conid()} ${getShortDate(open, timeZone)}"

  val date = s"${getShortDate(open, timeZone)}${timeZone.getID}"

  private var lastActive: Option[Instant] = None

  override def getSession(): String = toString()

  def setLastActive(lastAlive: Instant): Unit = {
    lastActive = Option(lastAlive)
  }

  def getLastActive: Option[Instant] = lastActive

  def marketState: String =
    if (isActive) "OPEN" else if (isPending) "PENDING" else "CLOSED"

  def isClosed: Boolean = Instant.now().isAfter(close)

  def isActive: Boolean =
    Instant.now().isAfter(open) && close.isAfter(Instant.now())

  def isPending: Boolean = open.isAfter(Instant.now())

  def elapsedSinceLastActive = {
    if (isActive) {
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

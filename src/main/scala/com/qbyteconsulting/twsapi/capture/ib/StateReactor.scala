package com.qbyteconsulting.twsapi.capture.ib

import java.time.{Clock, Duration, Instant}
import java.util.concurrent.ScheduledFuture

import com.qbyteconsulting.reactor.{Reactor, ReactorCore, ReactorEvent}

class StateReactor(val reactorCore: ReactorCore)
    extends Reactor
    with StatusListener {

  var connectionChecker: Option[ScheduledFuture[_]] = None

  override def onEvent(event: ReactorEvent): Unit = {
    event match {
      case ConnectionFail(_) | Status507(_) => {
        if (connectionChecker.isEmpty) {
          connectionChecker = Some(
            scheduleRepeat(ConnectionCheck(Instant.now()),
                           Duration.ofSeconds(15)))
        }
      }
      case TwsZoneId(zone) => {
        twsZoneId = zone
      }
      case _ => Unit
    }
  }

  override def connectionSuccess(): Unit = {
    publish(ConnectionSuccess())
    if (connectionChecker.isDefined) {
      val isCancelled = connectionChecker.get.cancel(true) // TODO side effect?
      if (isCancelled) connectionChecker = None
    }
  }

  override def clockSync(twsClock: Clock): Unit = publish(TwsClock(twsClock))

  override def connectionClosed(): Unit = publish(ConnectionClosed())

  override def notification(code: Int, msg: String): Unit = {
    val event = code match {
      case 502  => Status502()
      case 504  => Status504()
      case 507  => Status507()
      case 1100 => Status1100()
      case 1101 => Status1101()
      case 1102 => Status1102()
      case 2103 => {
        val farm =
          msg.substring(msg.indexOf(":") + 1, msg.length)
        Status2103(farm)
      }
      case _ => Status(code, msg)
    }
    publish(event)
  }

  override def error(requestId: Int, errorCode: Int, errorMsg: String): Unit =
    publish(RequestError(requestId, errorCode, errorMsg))
}

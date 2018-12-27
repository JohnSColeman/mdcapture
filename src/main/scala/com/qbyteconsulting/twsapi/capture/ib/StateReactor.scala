package com.qbyteconsulting.twsapi.capture.ib

import java.time.{Duration, Instant}
import java.util.concurrent.ScheduledFuture

import com.qbyteconsulting.twsapi.capture.ib.StateReactor.HealthCheck
import com.qbyteconsulting.twsapi.capture.reactor.{
  Reactor,
  ReactorCore,
  ReactorEvent
}

object StateReactor {

  case class HealthCheck(val startTime: Instant) extends ReactorEvent
}

class StateReactor(val reactorCore: ReactorCore)
    extends Reactor
    with StatusListener {

  var healthCheckCommand: Option[ScheduledFuture[_]] = None

  override def onEvent(event: ReactorEvent): Unit = {
    event match {
      case ConnectionFail(_) | Error507(_) => {
        if (healthCheckCommand.isEmpty) {
          healthCheckCommand = Some(
            scheduleRepeat(HealthCheck(Instant.now()), Duration.ofSeconds(15)))
        }
      }
      case _ => Unit
    }
  }

  override def connectionSuccess(): Unit = {
    publish(ConnectionSuccess())
    if (healthCheckCommand.isDefined) {
      val isCancelled = healthCheckCommand.get.cancel(true) // TODO side effect?
      if (isCancelled) healthCheckCommand = None
    }
  }

  override def connectionClosed(): Unit = publish(ConnectionClosed())

  override def error(errorCode: Int, errorMsg: String): Unit = {
    val event = errorCode match {
      case 502  => Error502()
      case 504  => Error504()
      case 507  => Error507()
      case 1100 => Error1100()
      case 1101 => Error1101()
      case 1102 => Error1102()
      case 2103 => {
        val farm =
          errorMsg.substring(errorMsg.indexOf(":") + 1, errorMsg.length)
        Error2103(farm)
      }
      case _ => Error(errorCode, errorMsg)
    }
    publish(event)
  }

  override def tickerError(tickerId: Int,
                           errorCode: Int,
                           errorMsg: String): Unit =
    publish(TickerError(tickerId, errorCode, errorMsg))
}

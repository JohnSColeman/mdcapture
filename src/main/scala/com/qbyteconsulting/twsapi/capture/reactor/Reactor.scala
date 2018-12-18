package com.qbyteconsulting.twsapi.capture.reactor

import java.util.concurrent.{Executors, TimeUnit}

object Reactor {

  class SheduledEvent(event: ReactorEvent, eventPublisher: EventPublisher)
      extends Runnable {
    override def run(): Unit = {
      eventPublisher.publishEvent(event)
    }
  }

  private def createSheduledEvent(event: ReactorEvent,
                                  eventPublisher: EventPublisher) = {
    new SheduledEvent(event, eventPublisher)
  }

  private val scheduledPool = Executors.newScheduledThreadPool(4);
}

trait Reactor extends EventValHandler with EventPublisher {
  import Reactor._

  val reactorCore: ReactorCore
  private val eventBuffer = reactorCore.eventBuffer

  final override def publishEvent(event: ReactorEvent): Unit = {
    val seq = eventBuffer.next()
    try {
      eventBuffer.get(seq).setValue(event)
      println(this.toString + " publishEvent:" + event)
    } finally {
      eventBuffer.publish(seq)
    }
  }

  final def scheduleEvent(event: ReactorEvent,
                          delay: Long,
                          timeUnit: TimeUnit = TimeUnit.MILLISECONDS): Unit = {
    scheduledPool.schedule(createSheduledEvent(event, this), delay, timeUnit)
  }
}

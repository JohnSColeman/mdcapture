package com.qbyteconsulting.reactor

import java.time.{Duration, Instant}
import java.util.concurrent.ScheduledFuture

trait Reactor extends EventValHandler with EventPublisher {

  val reactorCore: ReactorCore

  override def publish(event: ReactorEvent): Unit =
    reactorCore.schedule(event)

  def schedule(event: ReactorEvent, when: Instant): Unit =
    reactorCore.schedule(event, when)

  def schedule(event: ReactorEvent, delay: Duration): Unit =
    reactorCore.schedule(event, delay)

  def scheduleRepeat(event: ReactorEvent,
                     tickDuration: Duration): ScheduledFuture[_] =
    reactorCore.scheduleRepeat(event, tickDuration)
}

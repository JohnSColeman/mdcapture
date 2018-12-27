package com.qbyteconsulting.reactor

import java.time.{Duration, Instant}
import java.util.concurrent.{
  ScheduledFuture,
  ScheduledThreadPoolExecutor,
  TimeUnit
}

import com.lmax.disruptor.EventHandler
import com.lmax.disruptor.dsl.Disruptor
import org.slf4j.LoggerFactory

object ReactorCore {

  private val schedulerGroup =
    new ThreadGroup(Thread.currentThread().getThreadGroup, "scheduler")

  private val schedulerGroupThreadGroupFactory = new ThreadGroupFactory(
    schedulerGroup)
}

final class ReactorCore(name: String, size: Int) {
  import ReactorCore._

  private val log = LoggerFactory.getLogger(classOf[ReactorCore])

  private val debug = log.isDebugEnabled

  private val singleThreadScheduler =
    new ScheduledThreadPoolExecutor(1, schedulerGroupThreadGroupFactory)

  private val reactorGroup =
    new ThreadGroup(Thread.currentThread().getThreadGroup, name)

  private val disruptor =
    new Disruptor[EventVal](ReactorEventFactory,
                            size,
                            new ThreadGroupFactory(reactorGroup))

  private[reactor] val eventBuffer = disruptor.getRingBuffer

  def launch(eventHandlers: EventHandler[EventVal]*): ReactorCore = {
    disruptor.handleEventsWith(eventHandlers: _*)
    disruptor.start()
    val seq = eventBuffer.next
    try {
      eventBuffer.get(seq).setValue(Launch())
    } finally {
      eventBuffer.publish(seq)
    }
    this
  }

  private[reactor] def schedule(event: ReactorEvent): Unit =
    singleThreadScheduler.schedule(runnableEvent(event),
                                   0,
                                   TimeUnit.MILLISECONDS)

  private[reactor] def schedule(event: ReactorEvent, when: Instant): Unit =
    singleThreadScheduler.schedule(
      runnableEvent(event),
      when.toEpochMilli - System.currentTimeMillis(),
      TimeUnit.MILLISECONDS)

  private[reactor] def schedule(event: ReactorEvent, delay: Duration): Unit =
    singleThreadScheduler.schedule(runnableEvent(event),
                                   delay.toNanos,
                                   TimeUnit.MILLISECONDS)

  private[reactor] def scheduleRepeat(
      event: ReactorEvent,
      tickDuration: Duration): ScheduledFuture[_] =
    singleThreadScheduler.scheduleAtFixedRate(runnableEvent(event),
                                              0,
                                              tickDuration.toNanos,
                                              TimeUnit.NANOSECONDS)

  private def runnableEvent(event: ReactorEvent) =
    new Runnable {
      override def run(): Unit = {
        publish(event)
      }
    }

  private def publish(event: ReactorEvent): Unit = {
    val seq = eventBuffer.next()
    try {
      eventBuffer.get(seq).setValue(event)
      if (debug) { log.debug(s"$name publish: $event") }
    } finally {
      eventBuffer.publish(seq)
    }
  }
}

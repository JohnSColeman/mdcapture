package com.qbyteconsulting

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicLong

import com.lmax.disruptor.{EventFactory, EventHandler}
import org.slf4j.LoggerFactory

package object reactor {

  trait ReactorEvent
  case class Launch() extends ReactorEvent

  class EventVal() {

    private var value: ReactorEvent = null
    def getValue: ReactorEvent = value
    def setValue(value: ReactorEvent): Unit = this.value = value
  }

  private[reactor] def ReactorEventFactory: EventFactory[EventVal] =
    new EventFactory[EventVal]() {
      override def newInstance = new EventVal()
    }

  class ThreadGroupFactory(group: ThreadGroup) extends ThreadFactory {

    private val threadSeqNumber = new AtomicLong(1)

    private def nextName() =
      s"Reactor ${group.getName}-${threadSeqNumber.getAndIncrement()}"

    override def newThread(r: Runnable): Thread =
      new Thread(group, r, nextName())
  }
}

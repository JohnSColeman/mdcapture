package com.qbyteconsulting.twsapi.capture

import java.util.concurrent.ThreadFactory

import com.lmax.disruptor.{EventFactory, EventHandler}

package object reactor {

  trait ReactorEvent
  case class Launch() extends ReactorEvent

  class EventVal() {
    private var value: ReactorEvent = null
    def getValue: ReactorEvent = value
    def setValue(value: ReactorEvent): Unit = this.value = value
  }

  trait EventValHandler extends EventHandler[EventVal] {

    final override def onEvent(t: EventVal, l: Long, b: Boolean): Unit = {
      println(this.toString + " onEvent:" + t.getValue)
      handleEvent(t.getValue)
    }

    def handleEvent(event: ReactorEvent): Unit = ()
  }

  trait EventPublisher {
    def publishEvent(event: ReactorEvent): Unit
  }

  private[reactor] def REACTOR_EVENT_FACTORY: EventFactory[EventVal] =
    new EventFactory[EventVal]() {
      override def newInstance = new EventVal()
    }

  class DefaultThreadFactory extends ThreadFactory {
    override def newThread(r: Runnable): Thread = new Thread(r)
  }

  lazy val ThreadFactoryInstance: ThreadFactory =
    new DefaultThreadFactory
}

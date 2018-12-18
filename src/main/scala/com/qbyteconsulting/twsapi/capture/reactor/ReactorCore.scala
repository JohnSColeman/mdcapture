package com.qbyteconsulting.twsapi.capture.reactor

import com.lmax.disruptor.EventHandler
import com.lmax.disruptor.dsl.Disruptor

class ReactorCore(size: Int) {

  private val disruptor =
    new Disruptor[EventVal](REACTOR_EVENT_FACTORY, size, ThreadFactoryInstance)

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
}

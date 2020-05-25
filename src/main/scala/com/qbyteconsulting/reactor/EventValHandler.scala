package com.qbyteconsulting.reactor

import com.lmax.disruptor.EventHandler
import com.qbyteconsulting.LogTry
import org.slf4j.LoggerFactory

trait EventValHandler extends EventHandler[EventVal] {

  private implicit val log = LoggerFactory.getLogger(classOf[EventValHandler])

  private val debug = log.isDebugEnabled

  override def onEvent(t: EventVal, l: Long, b: Boolean): Unit = {
    if (debug) log.debug(t.getValue.toString)
    LogTry(onEvent(t.getValue))
  }

  def onEvent(event: ReactorEvent): Unit = ()
}

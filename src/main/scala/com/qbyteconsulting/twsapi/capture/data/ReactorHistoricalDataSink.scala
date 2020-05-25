package com.qbyteconsulting.twsapi.capture.data

import com.qbyteconsulting.reactor.{Reactor, ReactorCore, ReactorEvent}
import com.qbyteconsulting.twsapi.capture.HistoricalDataSink
import com.qbyteconsulting.twsapi.capture.ib.FinishHistoricalData

object ReactorHistoricalDataSink {

  trait HistoricalMarketEvent extends ReactorEvent {
    val tickerId: Int
  }

  case class HistoricalBar(tickerId: Int,
                           date: Long,
                           open: Double,
                           high: Double,
                           low: Double,
                           close: Double,
                           volume: Long,
                           count: Int)
    extends HistoricalMarketEvent

}

class ReactorHistoricalDataSink(val reactorCore: ReactorCore)
  extends HistoricalDataSink
    with Reactor {

  import ReactorHistoricalDataSink._

  override def processHistoricalData(reqId: Int,
                                     timestamp: Long,
                                     open: Double,
                                     high: Double,
                                     low: Double,
                                     close: Double,
                                     volume: Long,
                                     count: Int,
                                     wap: Double): Unit =
    publish(HistoricalBar(reqId, timestamp, open, high, low, close, volume, count))

  override def finishHistoricalData(reqId: Int,
                                    timestamp: Long,
                                    fromTime: Long,
                                    toTime: Long): Unit =
    publish(FinishHistoricalData(reqId, timestamp, fromTime, toTime))
}

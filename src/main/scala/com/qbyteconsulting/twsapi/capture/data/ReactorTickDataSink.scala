package com.qbyteconsulting.twsapi.capture.data

import com.qbyteconsulting.twsapi.capture.TickDataSink
import com.qbyteconsulting.twsapi.capture.reactor.{
  Reactor,
  ReactorCore,
  ReactorEvent
}

object ReactorTickDataSink {

  trait MarketEvent extends ReactorEvent
  case class BidPrice(val tickerId: Int,
                      val price: Double,
                      val timestamp: Long = System.currentTimeMillis())
      extends MarketEvent
  case class AskPrice(val tickerId: Int,
                      val price: Double,
                      val timestamp: Long = System.currentTimeMillis())
      extends MarketEvent
  case class LastPrice(val tickerId: Int,
                       val price: Double,
                       val timestamp: Long = System.currentTimeMillis())
      extends MarketEvent
  case class TradedVolume(val tickerId: Int,
                          val volume: Int,
                          val timestamp: Long = System.currentTimeMillis())
      extends MarketEvent
}

class ReactorTickDataSink(val reactorCore: ReactorCore)
    extends TickDataSink
    with Reactor {
  import ReactorTickDataSink._

  override def updateBidPrice(tickerId: Int,
                              price: Double,
                              timestamp: Long): Unit =
    publishEvent(BidPrice(tickerId, price, timestamp: Long))

  override def updateAskPrice(tickerId: Int,
                              price: Double,
                              timestamp: Long): Unit =
    publishEvent(AskPrice(tickerId, price, timestamp: Long))

  override def updateLastPrice(tickerId: Int,
                               price: Double,
                               timestamp: Long): Unit =
    publishEvent(LastPrice(tickerId, price, timestamp: Long))

  override def updateTradedVolume(tickerId: Int,
                                  volume: Int,
                                  timestamp: Long): Unit =
    publishEvent(TradedVolume(tickerId, volume, timestamp: Long))

  override def tickerCloses(tickerId: Int): Unit = ()
}

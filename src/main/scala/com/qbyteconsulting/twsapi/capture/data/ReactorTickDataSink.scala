package com.qbyteconsulting.twsapi.capture.data

import com.qbyteconsulting.twsapi.capture.TickDataSink
import com.qbyteconsulting.reactor.{Reactor, ReactorCore, ReactorEvent}

object ReactorTickDataSink {

  trait MarketEvent extends ReactorEvent { val tickerId: Int }
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
    publish(BidPrice(tickerId, price, timestamp))

  override def updateAskPrice(tickerId: Int,
                              price: Double,
                              timestamp: Long): Unit =
    publish(AskPrice(tickerId, price, timestamp))

  override def updateLastPrice(tickerId: Int,
                               price: Double,
                               timestamp: Long): Unit =
    publish(LastPrice(tickerId, price, timestamp))

  override def updateTradedVolume(tickerId: Int,
                                  volume: Int,
                                  timestamp: Long): Unit =
    publish(TradedVolume(tickerId, volume, timestamp))

  override def tickerCloses(tickerId: Int): Unit = {
    println(s"tickerCloses $tickerId")
  }
}

package com.qbyteconsulting.twsapi.capture.data

import java.util.Date

import com.qbyteconsulting.twsapi.capture.TickDataSink
import com.qbyteconsulting.reactor.{Reactor, ReactorCore, ReactorEvent}

object ReactorTickDataSink {

  trait TickMarketEvent extends ReactorEvent { val tickerId: Int }
  case class BidPrice(tickerId: Int,
                      price: Double,
                      timestamp: Long = System.currentTimeMillis())
      extends TickMarketEvent
  case class AskPrice(tickerId: Int,
                      price: Double,
                      timestamp: Long = System.currentTimeMillis())
      extends TickMarketEvent
  case class LastPrice(tickerId: Int,
                       price: Double,
                       timestamp: Long = System.currentTimeMillis())
      extends TickMarketEvent
  case class TradedVolume(tickerId: Int,
                          volume: Int,
                          timestamp: Long = System.currentTimeMillis())
      extends TickMarketEvent
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
    println(s"tickerCloses $tickerId at ${new Date()}")
  }
}

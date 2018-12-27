package com.qbyteconsulting.twsapi.capture.data

import com.qbyteconsulting.reactor.{EventValHandler, _}
import com.qbyteconsulting.twsapi.capture.data.ReactorTickDataSink.{
  AskPrice,
  BidPrice,
  LastPrice,
  TradedVolume
}
import org.slf4j.LoggerFactory

class LoggingTickDataEventHandler(logFormat: String = IdbLineFmt)
    extends EventValHandler {

  private val ticklog = LoggerFactory.getLogger("ticklog")

  override def onEvent(event: ReactorEvent): Unit = {
    event match {
      case BidPrice(tickerId, price, timestamp) =>
        ticklog.info(
          logFormat.format(tickerId, "bid", price, (timestamp * 1000)))
      case AskPrice(tickerId, price, timestamp) =>
        ticklog.info(
          logFormat.format(tickerId, "ask", price, (timestamp * 1000)))
      case LastPrice(tickerId, price, timestamp) =>
        ticklog.info(
          logFormat.format(tickerId, "last", price, (timestamp * 1000)))
      case TradedVolume(tickerId, volume, timestamp) =>
        ticklog.info(
          logFormat.format(tickerId, "volume", volume, (timestamp * 1000)))
      case _ => ()
    }
  }
}

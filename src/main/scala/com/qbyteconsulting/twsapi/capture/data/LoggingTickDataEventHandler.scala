package com.qbyteconsulting.twsapi.capture.data

import com.qbyteconsulting.twsapi.capture.data.ReactorTickDataSink.{
  AskPrice,
  BidPrice,
  LastPrice,
  TradedVolume
}
import com.qbyteconsulting.twsapi.capture.reactor
import com.qbyteconsulting.twsapi.capture.reactor.EventValHandler
import org.slf4j.LoggerFactory

class LoggingTickDataEventHandler(logFormat: String = IdbLineFmt)
    extends EventValHandler {

  private implicit val log =
    LoggerFactory.getLogger("ticklog")

  override def handleEvent(event: reactor.ReactorEvent): Unit = {
    event match {
      case BidPrice(tickerId, price, timestamp) =>
        log.info(logFormat.format(tickerId, "bid", price, (timestamp * 1000)))
      case AskPrice(tickerId, price, timestamp) =>
        log.info(logFormat.format(tickerId, "ask", price, (timestamp * 1000)))
      case LastPrice(tickerId, price, timestamp) =>
        log.info(logFormat.format(tickerId, "last", price, (timestamp * 1000)))
      case TradedVolume(tickerId, volume, timestamp) =>
        log.info(
          logFormat.format(tickerId, "volume", volume, (timestamp * 1000)))
      case _ => ()
    }
  }
}

package com.qbyteconsulting.twsapi.capture.data

import java.util.concurrent.TimeUnit

import com.qbyteconsulting.reactor.{EventValHandler, _}
import com.qbyteconsulting.twsapi.capture.data.ReactorTickDataSink.{
  AskPrice,
  BidPrice,
  LastPrice,
  TradedVolume
}
import org.influxdb.InfluxDB
import org.influxdb.dto.Point

class InfluxTickDataEventHandler(db: InfluxDB) extends EventValHandler {

  override def onEvent(event: ReactorEvent): Unit = {
    event match {
      case BidPrice(tickerId, price, timestamp) =>
        writeMeasurement(s"$tickerId", timestamp, "bid", price)
      case AskPrice(tickerId, price, timestamp) =>
        writeMeasurement(s"$tickerId", timestamp, "ask", price)
      case LastPrice(tickerId, price, timestamp) =>
        writeMeasurement(s"$tickerId", timestamp, "last", price)
      case TradedVolume(tickerId, volume, timestamp) =>
        writeMeasurement(s"$tickerId", timestamp, "volume", volume)
      case _ => ()
    }
  }

  private def writeMeasurement(measurement: String,
                               mTimestamp: Long,
                               mType: String,
                               mVal: Number) = {
    db.write(
      Point
        .measurement(measurement)
        .time(mTimestamp, TimeUnit.MILLISECONDS)
        .tag("type", mType)
        .addField("val", mVal)
        .build())
  }
}

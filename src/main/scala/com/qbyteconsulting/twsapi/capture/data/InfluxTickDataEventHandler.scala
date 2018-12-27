package com.qbyteconsulting.twsapi.capture.data

import java.util.concurrent.TimeUnit

import com.qbyteconsulting.twsapi.capture.data.ReactorTickDataSink.{
  AskPrice,
  BidPrice,
  LastPrice,
  TradedVolume
}
import com.qbyteconsulting.twsapi.capture.reactor
import com.qbyteconsulting.twsapi.capture.reactor.EventValHandler
import org.influxdb.InfluxDB
import org.influxdb.dto.Point

class InfluxTickDataEventHandler(influxDB: InfluxDB) extends EventValHandler {

  override def onEvent(event: reactor.ReactorEvent): Unit = {
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
    influxDB.write(
      Point
        .measurement(measurement)
        .time(mTimestamp, TimeUnit.MILLISECONDS)
        .addField("type", mType)
        .addField("val", mVal)
        .build())
  }
}

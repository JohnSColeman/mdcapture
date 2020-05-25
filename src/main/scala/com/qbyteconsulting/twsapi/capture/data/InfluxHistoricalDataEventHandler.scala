package com.qbyteconsulting.twsapi.capture.data

import java.util.concurrent.TimeUnit

import com.qbyteconsulting.reactor.{EventValHandler, _}
import com.qbyteconsulting.twsapi.capture.config.HistoricalData
import com.qbyteconsulting.twsapi.capture.data.ReactorHistoricalDataSink.HistoricalBar
import com.qbyteconsulting.twsapi.capture.ib.ContractDb.ConId
import com.qbyteconsulting.twsapi.capture.ib.{
  ContractsConfigured,
  FinishHistoricalData
}
import org.influxdb.InfluxDB
import org.influxdb.dto.Point

import scala.collection.concurrent.TrieMap

class InfluxHistoricalDataEventHandler(db: InfluxDB) extends EventValHandler {

  private val historicalDataMap = new TrieMap[ConId, HistoricalData]()

  override def onEvent(event: ReactorEvent): Unit = {
    event match {
      case ContractsConfigured(configs) =>
        configs
          .filter(cfg => cfg.historicalData.isDefined)
          .foreach(cfg =>
            historicalDataMap.update(cfg.conid, cfg.historicalData.get))
      case HistoricalBar(tickerId,
      date,
      open,
      high,
      low,
      close,
      volume,
      count) =>
        historicalDataMap.get(tickerId).map { hd =>
          writeMeasurement(s"$tickerId",
            hd.barSize.toString,
            date,
            open,
            high,
            low,
            close,
            volume)
        }
      case FinishHistoricalData(tickerId, timestamp, fromTime, toTime) =>
        writeFinish(s"$tickerId", historicalDataMap.get(tickerId).get.barSize.toString, timestamp, fromTime, toTime)
      case _ => ()
    }
  }

  private def writeMeasurement(measurement: String,
                               mSize: String,
                               mTimestamp: Long,
                               mOpen: Double,
                               mHigh: Double,
                               mLow: Double,
                               mClose: Double,
                               mVolume: Long) = {
    db.write(
      Point
        .measurement(measurement)
        .time(mTimestamp, TimeUnit.MILLISECONDS)
        .tag("type", mSize)
        .addField("open", mOpen)
        .addField("high", mHigh)
        .addField("low", mLow)
        .addField("close", mClose)
        .addField("volume", mVolume)
        .build())
  }

  private def writeFinish(measurement: String,
                          mSize: String,
                          mTimestamp: Long,
                          mFromTimestamp: Long,
                          mToTimestamp: Long) = {
    db.write(
      Point
        .measurement(measurement)
        .time(mTimestamp, TimeUnit.MILLISECONDS)
        .tag("type", mSize)
        .addField("from", mFromTimestamp)
        .addField("to", mToTimestamp)
        .build())
  }
}

package com.qbyteconsulting.twsapi.capture

trait HistoricalDataSink {

  def processHistoricalData(reqId: Int,
                            timestamp: Long,
                            open: Double,
                            high: Double,
                            low: Double,
                            close: Double,
                            volume: Long,
                            count: Int,
                            wap: Double)

  def finishHistoricalData(reqId: Int, timestamp: Long, fromTime: Long, toTime: Long)
}

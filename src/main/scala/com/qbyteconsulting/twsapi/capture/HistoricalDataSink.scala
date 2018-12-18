package com.qbyteconsulting.twsapi.capture

trait HistoricalDataSink {

  def processHistoricalData(reqId: Int,
                            date: Long,
                            open: Double,
                            high: Double,
                            low: Double,
                            close: Double,
                            volume: Int,
                            count: Int,
                            wap: Double,
                            hasGaps: Boolean)

  def finishHistoricalData(reqId: Int)
}

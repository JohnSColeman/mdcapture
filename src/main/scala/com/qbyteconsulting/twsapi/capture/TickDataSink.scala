package com.qbyteconsulting.twsapi.capture

trait TickDataSink {

  def updateBidPrice(tickerId: Int, price: Double, timestamp: Long)

  def updateAskPrice(tickerId: Int, price: Double, timestamp: Long)

  def updateLastPrice(tickerId: Int, price: Double, timestamp: Long)

  def updateTradedVolume(tickerId: Int, volume: Int, timestamp: Long)

  def tickerCloses(tickerId: Int)
}

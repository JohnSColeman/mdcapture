package com.qbyteconsulting.twsapi.capture.ib

trait StatusListener {

  def connectionSuccess()
  def connectionClosed()
  def error(errorCode: Int, errorMsg: String)
  def tickerError(tickerId: Int, code: Int, msg: String)
}

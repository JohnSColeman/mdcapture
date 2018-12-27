package com.qbyteconsulting.twsapi.capture.ib

trait StatusListener {

  def connectionSuccess()
  def connectionClosed()
  def notification(code: Int, msg: String)
  def error(requestId: Int, errorCode: Int, errorMsg: String)
}

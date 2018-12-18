package com.qbyteconsulting.twsapi.capture.ib

trait StatusListener {

  def connectionSuccess()
  def error(id: Int, errorCode: Int, errorMsg: String)
}

package com.qbyteconsulting.twsapi.capture.ib

import java.time.{Clock, ZoneId}
import java.util.TimeZone

trait StatusListener {
  var twsZoneId : ZoneId = TimeZone.getDefault.toZoneId

  def connectionSuccess()
  def connectionClosed()
  def clockSync(twsClock: Clock)
  def notification(code: Int, msg: String)
  def error(requestId: Int, errorCode: Int, errorMsg: String)
}

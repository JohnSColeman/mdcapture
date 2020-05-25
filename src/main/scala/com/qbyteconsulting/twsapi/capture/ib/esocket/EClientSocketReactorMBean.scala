package com.qbyteconsulting.twsapi.capture.ib.esocket

trait EClientSocketReactorMBean {

  def getConnected(): java.lang.Boolean
  def getConnectionError(): String
  def reconnect(): String
  def reload(): Unit
}

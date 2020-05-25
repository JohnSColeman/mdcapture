package com.qbyteconsulting.twsapi.capture.ib.esocket

import java.net.Socket
import java.util.concurrent.atomic.AtomicInteger

import com.ib.client.{EClientSocket, EReader, EReaderSignal, EWrapper}

class EClientSocketReader(eWrapper: EWrapper, signal: EReaderSignal)
    extends EClientSocket(eWrapper, signal) {

  private val sessionNumber = new AtomicInteger(1)
  private var reader: EReader = null
  private var consumer: Thread = null

  def connectSocket(socket: Socket, clientId: Int) = {
    if (!isConnected()) {
      println("connectSocket " + socket)
      cleanupOldThreads()
      eConnect(socket, clientId)
      startNewThreads()
    }
  }

  private def cleanupOldThreads() = {
    if (reader != null && reader.isAlive) {
      reader.interrupt()
      reader = null
    }
    if (consumer != null && consumer.isAlive) {
      signal.issueSignal()
      consumer.interrupt()
      consumer = null
    }
  }

  private def startNewThreads() = {
    val nextSessionNumber = sessionNumber.getAndIncrement().toString

    reader = new EReader(this, signal)
    reader.setName(s"EClientSocketReader-reader-${nextSessionNumber}")
    reader.start()

    consumer = new Thread() {
      setName(s"EClientSocketReader-consumer-${nextSessionNumber}")
      override def run(): Unit = {
        while (isConnected) {
          signal.waitForSignal
          try reader.processMsgs()
          catch {
            case e: Exception =>
              println("Exception from consumer thread: " + e.getMessage)
          }
        }
      }
    }
    consumer.start()
  }
}

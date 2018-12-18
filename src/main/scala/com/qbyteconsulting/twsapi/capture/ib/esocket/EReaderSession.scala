package com.qbyteconsulting.twsapi.capture.ib.esocket

import com.ib.client.{EClientSocket, EReader, EReaderSignal}

class EReaderSession(socket: EClientSocket, signal: EReaderSignal) {

  val reader = new EReader(socket, signal)
  reader.start()

  val consumer = new Thread() {
    override def run(): Unit = {
      while (socket.isConnected) {
        signal.waitForSignal
        try reader.processMsgs()
        catch {
          case e: Exception =>
            System.out.println("Exception: " + e.getMessage)
        }
      }
      reader.interrupt()
    }
  }
  consumer.start()

}

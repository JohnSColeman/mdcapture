package com.qbyteconsulting.twsapi.capture.ib.esocket

import java.lang
import java.lang.management.ManagementFactory
import java.net.{InetAddress, InetSocketAddress, Socket}

import com.ib.client.{EJavaSignal, EWrapper}
import com.qbyteconsulting.LogTry
import com.qbyteconsulting.reactor.{Launch, Reactor, ReactorCore, ReactorEvent}
import com.qbyteconsulting.twsapi.capture.ib._
import javax.management.ObjectName

import scala.util.{Failure, Success, Try}

object EClientSocketReactor {

  private val GTT = "225"
}

class EClientSocketReactor(hostParams: IbHostParams,
                           ewrapper: EWrapper,
                           val reactorCore: ReactorCore)
    extends Reactor
    with EClientSocketReactorMBean {
  import EClientSocketReactor._

  LogTry {
    val on =
      new ObjectName("com.qbyteconsulting.twsapi.capture:type=Connection")
    ManagementFactory
      .getPlatformMBeanServer()
      .registerMBean(this, on)
  }

  private var socket = new Socket()

  private val signal = new EJavaSignal();

  private val clientSocket = new EClientSocketReader(ewrapper, signal)

  private var timeDif: Long = 0

  private var connectionError: String = ""

  override def onEvent(event: ReactorEvent): Unit = {
    event match {
      case Launch() | Reconnect() | ConnectionCheck(_) => connectClientSocket()
      case ContractsConfigured(contracts) =>
        contracts.foreach { c =>
          clientSocket.reqContractDetails(c.cConid, c.toIbContract())
        }
      case ConnectionSuccess() => clientSocket.reqCurrentTime()
      case RequestMarketData(contract) =>
        clientSocket.reqMktData(contract.conid(), contract, GTT, false, null)
      case CancelMarketData(conid) =>
        clientSocket.cancelMktData(conid)
      case Status507(_) => clientSocket.eDisconnect()
      case _            => Unit
    }
  }

  override def getConnected(): lang.Boolean =
    clientSocket.synchronized(clientSocket.isConnected)

  override def getConnectionError(): String =
    connectionError.synchronized(connectionError)

  override def reconnect(): String = clientSocket.synchronized {
    if (!clientSocket.isConnected) {
      publish(Reconnect())
      "reconnecting"
    } else "connected"
  }

  override def reload(): Unit = publish(Reload())

  private def connectClientSocket() = {
    tryForConnectedSocket(hostParams) match {
      case Success(socket) =>
        LogTry {
          if (!clientSocket.isConnected)
            clientSocket.connectSocket(socket, hostParams.clientId)
        } match {
          case Success(_) => Unit
          case Failure(t) => {
            connectionError =
              s"failed client socket connection to ${hostParams} - ${t.getLocalizedMessage}"
            log.error(connectionError)
            publish(ConnectionFail(t))
          }
        }
      case Failure(t) => {
        connectionError =
          s"failed socket connection to ${hostParams} - ${t.getLocalizedMessage}"
        log.error(connectionError)
        publish(ConnectionFail(t))
      }
    }
  }

  private def tryForConnectedSocket(hostParams: IbHostParams): Try[Socket] = {
    if (!socket.isClosed && socket.isConnected) Success(socket)
    else {
      Try {
        socket = new Socket()
        val inetAddress = InetAddress.getByName(hostParams.host)
        val socketAddress =
          new InetSocketAddress(inetAddress, hostParams.port)
        socket.connect(socketAddress)
        socket
      }
    }
  }
}

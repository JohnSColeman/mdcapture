package com.qbyteconsulting.twsapi.capture.ib.esocket

import java.lang
import java.lang.management.ManagementFactory
import java.net.{InetAddress, InetSocketAddress, Socket}
import java.text.SimpleDateFormat
import java.util.GregorianCalendar

import com.ib.client.{EJavaSignal, EWrapper}
import com.qbyteconsulting.twsapi.capture.ib.StateReactor.HealthCheck
import com.qbyteconsulting.twsapi.capture.ib._
import com.qbyteconsulting.twsapi.capture.reactor.{
  Launch,
  Reactor,
  ReactorCore,
  ReactorEvent
}
import javax.management.{NotificationBroadcasterSupport, ObjectName}

import scala.util.{Failure, Success, Try}

object EClientSocketReactor {

  private val GTT = "225"

  class Notifier extends NotificationBroadcasterSupport {}
}

class EClientSocketReactor(hostParams: IbHostParams,
                           ewrapper: EWrapper,
                           val reactorCore: ReactorCore)
    extends Reactor
    with EClientSocketReactorMBean {
  import EClientSocketReactor._
  import com.qbyteconsulting.twsapi.capture.LogTry

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
      case Launch() | Reconnect() | HealthCheck(_) => connectClientSocket()
      case ContractsConfigured(contracts) =>
        contracts.foreach { c =>
          clientSocket.reqContractDetails(c.cConid, c.toIbContract())
        }
      case RequestMarketData(contract) =>
        clientSocket.reqMktData(contract.conid(), contract, GTT, false, null)
      case CancelMarketData(conid) =>
        clientSocket.cancelMktData(conid)
      case Error507(_) => clientSocket.eDisconnect()
      case _           => Unit
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
          case Success(_) => publish(ServerTimeAdjust(calcDif()))
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

  private def calcDif(): Long = { // TODO flaky
    val twsdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss z")
    val twsTime: Long = twsdf.parse(clientSocket.TwsConnectionTime).getTime
    val twsCal = new GregorianCalendar
    twsCal.setTimeInMillis(twsTime)
    val pcTime: Long = System.currentTimeMillis
    val calcTimeIDif = (pcTime - twsTime) // if price server later timeDif -ve
    println(s"PC-Trader Workstation time dif = ${calcTimeIDif} ms")
    calcTimeIDif
  }
}

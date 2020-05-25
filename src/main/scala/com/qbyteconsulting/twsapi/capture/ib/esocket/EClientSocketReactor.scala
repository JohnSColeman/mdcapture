package com.qbyteconsulting.twsapi.capture.ib.esocket

import java.lang
import java.lang.management.ManagementFactory
import java.net.{InetAddress, InetSocketAddress, Socket}
import java.text.SimpleDateFormat
import java.time.{ZoneId, ZonedDateTime}
import java.time.format.DateTimeFormatter

import com.ib.client.{EJavaSignal, EWrapper, Types}
import com.qbyteconsulting.LogTry
import com.qbyteconsulting.reactor._
import com.qbyteconsulting.twsapi.capture.ib._
import javax.management.ObjectName
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}

object EClientSocketReactor {

  private val TickList = "225"

  val TwsConnectionTimeFmt = "yyyyMMdd HH:mm:ss z"

  def extractZoneId(twsConnectionTime: String): ZoneId = {
    val fmt = new SimpleDateFormat(TwsConnectionTimeFmt)
    fmt.parse(twsConnectionTime)
    fmt.getTimeZone.toZoneId
  }
}

class EClientSocketReactor(hostParams: IbHostParams,
                           ewrapper: EWrapper,
                           val reactorCore: ReactorCore)
  extends Reactor
    with EClientSocketReactorMBean {

  import EClientSocketReactor._

  private implicit val log = LoggerFactory.getLogger(classOf[EClientSocketReactor])

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
          val contract = c.toIbContract()
          clientSocket.reqContractDetails(c.conid, contract)
          if (c.historicalData.isDefined) {
            val whatToShow = if (contract.secType() != Types.SecType.CASH) "TRADES" else "ASK"
            clientSocket.reqHeadTimestamp(c.conid, contract, whatToShow, 0, 1)
          }
        }
      case ConnectionSuccess() => {
        val twsConnectionTime = clientSocket.getTwsConnectionTime
        val twsZoneId = extractZoneId(twsConnectionTime)
        println(s"TWS connection time zone [$twsZoneId]")
        publish(TwsZoneId(twsZoneId))
        clientSocket.reqCurrentTime()
      }
      case RequestMarketData(session) =>
        clientSocket.reqMktData(session.contract.conid(),
          session.contract,
          TickList,
          false,
          false,
          null)
      case CancelMarketData(session) =>
        clientSocket.cancelMktData(session.contract.conid())
      case RequestHistoricalData(tickerId,
      contract,
      endDateTime,
      durationStr,
      barSizeSetting,
      whatToShow,
      useRTH,
      formatDate,
      keepUpToDate) =>
        clientSocket.reqHistoricalData(tickerId,
          contract,
          endDateTime,
          durationStr,
          barSizeSetting,
          whatToShow,
          useRTH,
          formatDate,
          keepUpToDate,
          null)
      case Status507(_) => clientSocket.eDisconnect()
      case _ => Unit
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

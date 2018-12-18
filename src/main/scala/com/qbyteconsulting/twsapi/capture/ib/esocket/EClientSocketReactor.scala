package com.qbyteconsulting.twsapi.capture.ib.esocket

import java.net.{InetAddress, InetSocketAddress, Socket}
import java.text.SimpleDateFormat
import java.util.GregorianCalendar

import com.ib.client.{EClientSocket, EJavaSignal, EWrapper}
import com.qbyteconsulting.twsapi.capture.ib._
import com.qbyteconsulting.twsapi.capture.reactor.{
  Launch,
  Reactor,
  ReactorCore,
  ReactorEvent
}
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}

object EClientSocketReactor {

  private val GTT = "225"
}

class EClientSocketReactor(hostParams: IbHostParams,
                           ewrapper: EWrapper,
                           val reactorCore: ReactorCore)
    extends Reactor {
  import EClientSocketReactor._

  private implicit val log =
    LoggerFactory.getLogger(classOf[EClientSocketReactor])

  private var socket = new Socket()

  private val signal = new EJavaSignal();

  private val clientSocket = new EClientSocket(ewrapper, signal)

  private var timeDif: Long = 0

  override def handleEvent(event: ReactorEvent): Unit = {
    event match {
      case Launch() | Reconnect() => connectClientSocket()
      case ContractsConfigured(contracts) =>
        contracts.foreach { c =>
          clientSocket.reqContractDetails(c.cConid, c.toIbContract())
        }
      case RequestMarketData(contract) =>
        clientSocket.reqMktData(contract.conid(), contract, GTT, false, null)
      case CancelMarketData(conid) =>
        clientSocket.cancelMktData(conid)
      case _ => ()
    }
  }

  private def connectClientSocket() = this.synchronized {
    tryForConnectedSocket(hostParams) match {
      case Success(socket) =>
        LogTry {
          if (!clientSocket.isConnected) {
            clientSocket.eConnect(socket, hostParams.clientId)
            new EReaderSession(clientSocket, signal)
          }
        } match {
          case Success(_) => publishEvent(ServerTimeAdjust(calcDif()))
          case Failure(t) => {
            log.error(
              s"failed client socket connection to ${hostParams} - ${t.getLocalizedMessage}")
            publishEvent(ConnectionFail(t))
          }
        }
      case Failure(t) => {
        log.error(
          s"failed socket connection to ${hostParams} - ${t.getLocalizedMessage}")
        publishEvent(ConnectionFail(t))
      }
    }
  }

  private def tryForConnectedSocket(hostParams: IbHostParams): Try[Socket] = {
    if (socket.isConnected) Success(socket)
    else {
      Try {
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

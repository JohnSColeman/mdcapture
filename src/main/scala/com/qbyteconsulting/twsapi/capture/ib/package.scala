package com.qbyteconsulting.twsapi.capture

import com.ib.client.{Contract, ContractDetails}
import com.qbyteconsulting.twsapi.capture.config.ContractConfig
import com.qbyteconsulting.twsapi.capture.ib.ContractDb.ConId
import com.qbyteconsulting.twsapi.capture.reactor.ReactorEvent
import org.slf4j.Logger

import scala.util.{Failure, Try}

package object ib {

  def LogTry[A](block: => Unit)(implicit log: Logger): Try[Unit] = {
    Try(block) recoverWith {
      case e: Throwable =>
        log.error("Try error", e)
        Failure(e)
    }
  }

  case class IbHostParams(host: String = "127.0.0.1",
                          port: Int = 7496,
                          clientId: Int = 0)

  case class ConnectionSuccess() extends ReactorEvent
  case class ConnectionFail(t: Throwable) extends ReactorEvent
  case class Reconnect() extends ReactorEvent
  case class ServerTimeAdjust(val adjust: Long) extends ReactorEvent
  case class ErrorStatus(id: Int, errorCode: Int, errorMsg: String)
      extends ReactorEvent
  case class Status502(val message: String = "Couldn't connect to TWS.")
      extends ReactorEvent
  case class Status504(val message: String = "Not connected to TWS.")
      extends ReactorEvent
  case class Status1100(val message: String =
    "Connectivity between IB and the TWS has been lost.")
      extends ReactorEvent
  case class Status1101(
      val message: String =
        "Connectivity between IB and TWS has been restored- data lost.")
      extends ReactorEvent
  case class Status1102(
      val message: String =
        "Connectivity between IB and TWS has been restored- data maintained.")
      extends ReactorEvent
  case class Status2103(val farm: String,
                        val message: String =
                          "A market data farm is disconnected.")
      extends ReactorEvent
  case class Status2104(
      val message: String = "Market data farm connection is OK")
      extends ReactorEvent
  case class ContractsConfigured(val contracts: Iterable[ContractConfig])
      extends ReactorEvent
  case class ContractLoaded(val contractDetails: ContractDetails)
      extends ReactorEvent
  case class RequestMarketData(val contract: Contract) extends ReactorEvent
  case class CancelMarketData(val conid: ConId) extends ReactorEvent
}

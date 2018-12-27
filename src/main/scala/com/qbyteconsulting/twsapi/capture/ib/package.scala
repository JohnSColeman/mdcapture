package com.qbyteconsulting.twsapi.capture

import java.time.Instant

import com.ib.client.{Contract, ContractDetails}
import com.qbyteconsulting.reactor.ReactorEvent
import com.qbyteconsulting.twsapi.capture.config.ContractConfig
import com.qbyteconsulting.twsapi.capture.ib.ContractDb.ConId

package object ib {

  case class IbHostParams(host: String = "127.0.0.1",
                          port: Int = 7496,
                          clientId: Int = 0)

  case class ConnectionSuccess() extends ReactorEvent
  case class ConnectionClosed() extends ReactorEvent
  case class ConnectionFail(t: Throwable) extends ReactorEvent
  case class Reconnect() extends ReactorEvent
  case class Reload() extends ReactorEvent
  case class ConnectionCheck(val startTime: Instant) extends ReactorEvent
  case class RequestError(requestId: Int, errorCode: Int, errorMsg: String)
      extends ReactorEvent
  case class Status(code: Int, msg: String) extends ReactorEvent
  case class Status502(val message: String = "Couldn't connect to TWS.")
      extends ReactorEvent
  case class Status504(val message: String = "Not connected to TWS.")
      extends ReactorEvent
  case class Status507(val message: String = "Bad Message Length")
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

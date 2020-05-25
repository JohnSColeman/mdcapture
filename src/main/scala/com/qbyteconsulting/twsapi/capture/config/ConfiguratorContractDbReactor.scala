package com.qbyteconsulting.twsapi.capture.config

import com.ib.client.{Contract, ContractDetails}
import com.qbyteconsulting.reactor.{Reactor, ReactorCore, ReactorEvent}
import com.qbyteconsulting.twsapi.capture.ib.ContractDb.{ConId, ContractDbIOException}
import com.qbyteconsulting.twsapi.capture.ib._
import pureconfig.loadConfigOrThrow

import scala.collection.concurrent.TrieMap

object ConfiguratorContractDbReactor {

  private val configContainer = loadConfigOrThrow[ContractConfigContainer]
}

class ConfiguratorContractDbReactor(val reactorCore: ReactorCore)
  extends ContractDb
    with ContractListener
    with Reactor {

  import ConfiguratorContractDbReactor._

  private val contractDetailsCache = new TrieMap[ConId, ContractDetails]()

  private val configuredContracts = configContainer.contractConfigs

  override def onEvent(event: ReactorEvent): Unit = {
    event match {
      case ConnectionSuccess() | Reload() =>
        if (!configuredContracts.isEmpty)
          publish(ContractsConfigured(configuredContracts))
      case _ => Unit
    }
  }

  override def getContractDescriptions
  : Either[ContractDbIOException, Iterable[ContractDetails]] =
    Right(contractDetailsCache.values)

  override def getContracts: Either[ContractDbIOException, Iterable[Contract]] =
    Right(contractDetailsCache.values.map(_.contract()))

  override def getContractDescription(conid: ConId): Either[ContractDbIOException, Option[ContractDetails]] =
    Right(contractDetailsCache.get(conid))

  override def getContract(conid: ConId): Either[ContractDbIOException, Option[Contract]] =
    Right(contractDetailsCache.get(conid).map(_.contract()))

  override def storeContractDetails(contractDetails: ContractDetails): Either[ContractDbIOException, ConId] =
    Right {
      contractDetailsCache.update(contractDetails.conid(), contractDetails)
      contractDetails.conid()
    }

  override def updateContractDetails(conid: ConId,
                                     contractDetails: ContractDetails): Unit = {
    storeContractDetails(contractDetails) match {
      case Right(conId) =>
        publish(ContractDetailsLoaded(contractDetails))
      case Left(e) => e.printStackTrace()
    }
  }

  override def endContractDetails(conid: ConId): Unit = ()
}

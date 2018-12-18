package com.qbyteconsulting.twsapi.capture.ib

import java.io.IOException

import com.ib.client.{Contract, ContractDetails}

object ContractDb {

  type ConId = Int

  class ContractDbIOException() extends IOException()
}

trait ContractDb {
  import ContractDb._

  def getContractDescriptions
    : Either[ContractDbIOException, Iterable[ContractDetails]]

  def getContracts: Either[ContractDbIOException, Iterable[Contract]]

  def getContractDescription(
      conid: ConId): Either[ContractDbIOException, Option[ContractDetails]]

  def getContract(conid: ConId): Either[ContractDbIOException, Option[Contract]]

  def storeContractDetails(
      contractDetails: ContractDetails): Either[ContractDbIOException, ConId]
}

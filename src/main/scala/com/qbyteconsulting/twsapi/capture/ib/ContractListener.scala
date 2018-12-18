package com.qbyteconsulting.twsapi.capture.ib

import com.ib.client.ContractDetails
import com.qbyteconsulting.twsapi.capture.ib.ContractDb.ConId

trait ContractListener {

  def updateContractDetails(conid: ConId, contractDetails: ContractDetails)

  def endContractDetails(conid: ConId)
}

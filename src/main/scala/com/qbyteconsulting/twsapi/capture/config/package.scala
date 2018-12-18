package com.qbyteconsulting.twsapi.capture

import com.ib.client.Contract

package object config {

  case class ContractConfigContainer(contractConfigs: List[ContractConfig])

  case class ContractConfig(val cConid: Int = 0,
                            val cSymbol: String = "",
                            val cSecType: String = "",
                            val cLastTradeDateOrContractMonth: String = "",
                            val cStrike: Double = 0,
                            val cRight: String = "",
                            val cMultiplier: String = "",
                            val cExchange: String = "",
                            val cCurrency: String = "",
                            val cIncludeExpired: Boolean = false,
                            val cLocalSymbol: String = "",
                            val cTradingClass: String = "",
                            //    val cComboLegs: util.ArrayList[ComboLeg] = null,
                            val cPrimaryExch: String = "",
                            val cSecIdType: String = "",
                            val cSecId: String = "") {

    def toIbContract(): Contract = {
      val contract = new Contract()
      contract.conid(cConid)
      contract.symbol(cSymbol)
      contract.secType(cSecType)
      contract.lastTradeDateOrContractMonth(cLastTradeDateOrContractMonth)
      contract.strike(cStrike)
      contract.right(cRight)
      contract.multiplier(cMultiplier)
      contract.exchange(cExchange)
      contract.currency(cCurrency)
      contract.localSymbol(cLocalSymbol)
      contract.tradingClass(cTradingClass)
// TODO contract.comboLegs()
      contract.primaryExch(cPrimaryExch)
      contract.includeExpired(cIncludeExpired)
      contract.secIdType(cSecIdType)
      contract.secId(cSecId)
      contract
    }

    override def toString(): String = {
      s"conid#${cConid} symbol:${cSymbol} secType:${cSecId} strike:${cStrike} exchange:${cExchange} currency:${cCurrency}"
    }
  }
}

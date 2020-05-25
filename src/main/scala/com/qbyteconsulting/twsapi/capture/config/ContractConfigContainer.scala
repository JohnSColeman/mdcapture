package com.qbyteconsulting.twsapi.capture.config

import com.ib.client.Contract

final case class ContractConfigContainer(contractConfigs: List[ContractConfig])

final case class ContractConfig(conid: Int = 0,
                          symbol: String = "",
                          cSecType: String = "",
                          lastTradeDateOrContractMonth: String = "",
                          strike: Double = 0,
                          right: String = "",
                          multiplier: String = "",
                          exchange: String = "",
                          currency: String = "",
                          includeExpired: Boolean = false,
                          localSymbol: String = "",
                          tradingClass: String = "",
                          // TODO cComboLegs: util.ArrayList[ComboLeg] = null,
                          primaryExch: String = "",
                          secIdType: String = "",
                          secId: String = "",
                          historicalData: Option[HistoricalData] = None) {

  def toIbContract(): Contract = {
    val contract = new Contract()
    contract.conid(conid)
    contract.symbol(symbol)
    contract.secType(cSecType)
    contract.lastTradeDateOrContractMonth(lastTradeDateOrContractMonth)
    contract.strike(strike)
    contract.right(right)
    contract.multiplier(multiplier)
    contract.exchange(exchange)
    contract.currency(currency)
    contract.localSymbol(localSymbol)
    contract.tradingClass(tradingClass)
    // TODO contract.comboLegs()
    contract.primaryExch(primaryExch)
    contract.includeExpired(includeExpired)
    contract.secIdType(secIdType)
    contract.secId(secId)
    contract
  }

  override def toString(): String = {
    s"conid#${conid} symbol:${symbol} secType:${secId} strike:${strike} exchange:${exchange} currency:${currency}"
  }
}

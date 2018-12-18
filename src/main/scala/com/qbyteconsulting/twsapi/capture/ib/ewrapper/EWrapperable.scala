package com.qbyteconsulting.twsapi.capture.ib.ewrapper

import java.{lang, util}

import com.ib.client._

class EWrapperable extends EWrapper {

  override def tickPrice(tickerId: Int,
                         field: Int,
                         price: Double,
                         canAutoExecute: Int): Unit = {}

  override def tickSize(tickerId: Int, field: Int, size: Int): Unit = {}

  override def tickOptionComputation(tickerId: Int,
                                     field: Int,
                                     impliedVol: Double,
                                     delta: Double,
                                     optPrice: Double,
                                     pvDividend: Double,
                                     gamma: Double,
                                     vega: Double,
                                     theta: Double,
                                     undPrice: Double): Unit = {}

  override def tickGeneric(tickerId: Int,
                           tickType: Int,
                           value: Double): Unit = {}

  override def tickString(tickerId: Int, tickType: Int, value: String): Unit = {}

  override def tickEFP(tickerId: Int,
                       tickType: Int,
                       basisPoints: Double,
                       formattedBasisPoints: String,
                       impliedFuture: Double,
                       holdDays: Int,
                       futureLastTradeDate: String,
                       dividendImpact: Double,
                       dividendsToLastTradeDate: Double): Unit = {}

  override def orderStatus(orderId: Int,
                           status: String,
                           filled: Double,
                           remaining: Double,
                           avgFillPrice: Double,
                           permId: Int,
                           parentId: Int,
                           lastFillPrice: Double,
                           clientId: Int,
                           whyHeld: String): Unit = {}

  override def openOrder(orderId: Int,
                         contract: Contract,
                         order: Order,
                         orderState: OrderState): Unit = {}

  override def openOrderEnd(): Unit = {}

  override def updateAccountValue(key: String,
                                  value: String,
                                  currency: String,
                                  accountName: String): Unit = {}

  override def updatePortfolio(contract: Contract,
                               position: Double,
                               marketPrice: Double,
                               marketValue: Double,
                               averageCost: Double,
                               unrealizedPNL: Double,
                               realizedPNL: Double,
                               accountName: String): Unit = {}

  override def updateAccountTime(timeStamp: String): Unit = {}

  override def accountDownloadEnd(accountName: String): Unit = {}

  override def nextValidId(orderId: Int): Unit = {}

  override def contractDetails(reqId: Int,
                               contractDetails: ContractDetails): Unit = {}

  override def bondContractDetails(reqId: Int,
                                   contractDetails: ContractDetails): Unit = {}

  override def contractDetailsEnd(reqId: Int): Unit = {}

  override def execDetails(reqId: Int,
                           contract: Contract,
                           execution: Execution): Unit = {}

  override def execDetailsEnd(reqId: Int): Unit = {}

  override def updateMktDepth(tickerId: Int,
                              position: Int,
                              operation: Int,
                              side: Int,
                              price: Double,
                              size: Int): Unit = {}

  override def updateMktDepthL2(tickerId: Int,
                                position: Int,
                                marketMaker: String,
                                operation: Int,
                                side: Int,
                                price: Double,
                                size: Int): Unit = {}

  override def updateNewsBulletin(msgId: Int,
                                  msgType: Int,
                                  message: String,
                                  origExchange: String): Unit = {}

  override def managedAccounts(accountsList: String): Unit = {}

  override def receiveFA(faDataType: Int, xml: String): Unit = {}

  override def historicalData(reqId: Int,
                              date: String,
                              open: Double,
                              high: Double,
                              low: Double,
                              close: Double,
                              volume: Int,
                              count: Int,
                              WAP: Double,
                              hasGaps: Boolean): Unit = {}

  override def scannerParameters(xml: String): Unit = {}

  override def scannerData(reqId: Int,
                           rank: Int,
                           contractDetails: ContractDetails,
                           distance: String,
                           benchmark: String,
                           projection: String,
                           legsStr: String): Unit = {}

  override def scannerDataEnd(reqId: Int): Unit = {}

  override def realtimeBar(reqId: Int,
                           time: Long,
                           open: Double,
                           high: Double,
                           low: Double,
                           close: Double,
                           volume: Long,
                           wap: Double,
                           count: Int): Unit = {}

  override def currentTime(time: Long): Unit = {}

  override def fundamentalData(reqId: Int, data: String): Unit = {}

  override def deltaNeutralValidation(
      reqId: Int,
      underComp: DeltaNeutralContract): Unit = {}

  override def tickSnapshotEnd(reqId: Int): Unit = {}

  override def marketDataType(reqId: Int, marketDataType: Int): Unit = {}

  override def commissionReport(commissionReport: CommissionReport): Unit = {}

  override def position(account: String,
                        contract: Contract,
                        pos: Double,
                        avgCost: Double): Unit = {}

  override def positionEnd(): Unit = {}

  override def accountSummary(reqId: Int,
                              account: String,
                              tag: String,
                              value: String,
                              currency: String): Unit = {}

  override def accountSummaryEnd(reqId: Int): Unit = {}

  override def verifyMessageAPI(apiData: String): Unit = {}

  override def verifyCompleted(isSuccessful: Boolean,
                               errorText: String): Unit = {}

  override def verifyAndAuthMessageAPI(apiData: String,
                                       xyzChallange: String): Unit = {}

  override def verifyAndAuthCompleted(isSuccessful: Boolean,
                                      errorText: String): Unit = {}

  override def displayGroupList(reqId: Int, groups: String): Unit = {}

  override def displayGroupUpdated(reqId: Int, contractInfo: String): Unit = {}

  override def error(e: Exception): Unit = {}

  override def error(str: String): Unit = {}

  override def error(id: Int, errorCode: Int, errorMsg: String): Unit = {}

  override def connectionClosed(): Unit = {}

  override def connectAck(): Unit = {}

  override def positionMulti(reqId: Int,
                             account: String,
                             modelCode: String,
                             contract: Contract,
                             pos: Double,
                             avgCost: Double): Unit = {}

  override def positionMultiEnd(reqId: Int): Unit = {}

  override def accountUpdateMulti(reqId: Int,
                                  account: String,
                                  modelCode: String,
                                  key: String,
                                  value: String,
                                  currency: String): Unit = {}

  override def accountUpdateMultiEnd(reqId: Int): Unit = {}

  override def securityDefinitionOptionalParameter(
      reqId: Int,
      exchange: String,
      underlyingConId: Int,
      tradingClass: String,
      multiplier: String,
      expirations: util.Set[String],
      strikes: util.Set[lang.Double]): Unit = {}

  override def securityDefinitionOptionalParameterEnd(reqId: Int): Unit = {}

  override def softDollarTiers(reqId: Int,
                               tiers: Array[SoftDollarTier]): Unit = {}
}

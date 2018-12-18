package com.qbyteconsulting.twsapi.capture.ib.ewrapper

import com.ib.client.ContractDetails
import com.qbyteconsulting.twsapi.capture.TickDataSink
import com.qbyteconsulting.twsapi.capture.ib.{
  ContractListener,
  StatusListener,
  _
}
import org.slf4j.LoggerFactory

class ListeningEWrapperable(val contractListener: ContractListener,
                            val statusListener: StatusListener,
                            val dataSink: TickDataSink)
//  val historicalDataSink: HistoricalDataSink,
    extends EWrapperable {

  implicit val log = LoggerFactory.getLogger(classOf[ListeningEWrapperable])

  override def connectAck(): Unit = LogTry {
    statusListener.connectionSuccess()
  }

  override def error(id: Int, errorCode: Int, errorMsg: String): Unit = LogTry {
    statusListener.error(id, errorCode, errorMsg)
  }

  override def contractDetails(conid: Int,
                               contractDetails: ContractDetails): Unit =
    LogTry {
      contractListener.updateContractDetails(conid, contractDetails)
    }

  override def bondContractDetails(conid: Int,
                                   contractDetails: ContractDetails): Unit =
    LogTry {
      contractListener.updateContractDetails(conid, contractDetails)
    }

  override def contractDetailsEnd(conid: Int): Unit = LogTry {
    contractListener.endContractDetails(conid)
  }

  override def tickPrice(tickerId: Int,
                         field: Int,
                         price: Double,
                         canAutoExecute: Int): Unit = LogTry {
    val timestamp = System.currentTimeMillis()
    if (price != -1) {
      field match {
        case 1 => dataSink.updateBidPrice(tickerId, price, timestamp)
        case 2 => dataSink.updateAskPrice(tickerId, price, timestamp)
        case 4 => dataSink.updateLastPrice(tickerId, price, timestamp)
        case _ => ()
      }
    } else dataSink.tickerCloses(tickerId)
  }

  override def tickSize(tickerId: Int, field: Int, size: Int): Unit = LogTry {
    val timestamp = System.currentTimeMillis()
    field match {
      case 8 => dataSink.updateTradedVolume(tickerId, size, timestamp)
      case _ => ()
    }
  }

  /** TODO override def historicalData(reqId: Int,
                              date: String,
                              open: Double,
                              high: Double,
                              low: Double,
                              close: Double,
                              volume: Int,
                              count: Int,
                              wap: Double,
                              hasGaps: Boolean): Unit =
    LogTry {
      if (open != 0 && high != 0 && low != 0 && close != 0 && volume != 0) {
        if (!date.startsWith("finished-")) {
          if (date.length() == 8) { // yyyyMMdd format
            val cal = HDDF.parse(date).getTime() / 1000
            historicalDataSink.processHistoricalData(reqId,
              cal,
              open,
              high,
              low,
              close,
              volume,
              count,
              wap,
              hasGaps)
          } else {
            val cal = date.toLong * 1000
            historicalDataSink.processHistoricalData(reqId,
              cal,
              open,
              high,
              low,
              close,
              volume,
              count,
              wap,
              hasGaps)
          }
        } else {
          historicalDataSink.finishHistoricalData(reqId)
        }
      }
    } */

}

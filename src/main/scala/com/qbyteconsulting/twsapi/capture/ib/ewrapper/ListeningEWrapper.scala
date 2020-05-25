package com.qbyteconsulting.twsapi.capture.ib.ewrapper

import java.time.{Clock, Duration}

import com.ib.client._
import com.qbyteconsulting.LogTry
import com.qbyteconsulting.twsapi.capture.ib.{ContractListener, StatusListener, toTimeMillis}
import com.qbyteconsulting.twsapi.capture.{HistoricalDataSink, TickDataSink}
import org.slf4j.LoggerFactory

class ListeningEWrapper(val contractListener: ContractListener,
                        val statusListener: StatusListener,
                        val tickDataSink: TickDataSink,
                        val historicalDataSink: HistoricalDataSink)
  extends DefaultEWrapper {

  implicit val log = LoggerFactory.getLogger(classOf[ListeningEWrapper])

  private var twsClock: Clock = _

  override def connectAck(): Unit = LogTry {
    statusListener.connectionSuccess()
  }

  override def connectionClosed(): Unit = LogTry {
    statusListener.connectionClosed()
  }

  override def currentTime(time: Long): Unit = LogTry {
    val epochMillis = time * 1000
    val timeDif = epochMillis - System.currentTimeMillis()
    twsClock =
      Clock.offset(Clock.system(statusListener.twsZoneId), Duration.ofMillis(timeDif))
    println(s"TWS clock $twsClock")
    log.info(s"TWS clock $twsClock")
    statusListener.clockSync(twsClock)
  }

  override def error(id: Int, errorCode: Int, errorMsg: String): Unit = LogTry {
    log.warn(s"error: [$id] #$errorCode - $errorMsg")
    if (id == -1) statusListener.notification(errorCode, errorMsg)
    else statusListener.error(id, errorCode, errorMsg)
  }

  override def error(e: Exception): Unit = {
    log.warn("received exception: ", e)
  }

  override def error(str: String): Unit = {
    log.warn(s"received error: $str")
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
                         attribs: TickAttrib): Unit = LogTry {
    val timestamp = twsClock.millis()
    if (price != -1) {
      field match {
        case 1 => tickDataSink.updateBidPrice(tickerId, price, timestamp)
        case 2 => tickDataSink.updateAskPrice(tickerId, price, timestamp)
        case 4 => tickDataSink.updateLastPrice(tickerId, price, timestamp)
        case _ => ()
      }
    } else tickDataSink.tickerCloses(tickerId)
  }

  override def tickSize(tickerId: Int, field: Int, size: Int): Unit = LogTry {
    val timestamp = twsClock.millis()
    field match {
      case 8 => tickDataSink.updateTradedVolume(tickerId, size, timestamp)
      case _ => ()
    }
  }

  override def headTimestamp(reqId: Int, headTimestamp: String): Unit = LogTry {
    val headTime = toTimeMillis(headTimestamp, statusListener.twsZoneId)
    println(s"$reqId head timestamp $headTimestamp ${headTime}ms")
  }

  override def historicalData(reqId: Int, bar: Bar): Unit = LogTry {
    historicalDataSink.processHistoricalData(reqId,
      toTimeMillis(bar.time(), statusListener.twsZoneId),
      bar.open(),
      bar.high(),
      bar.low(),
      bar.close(),
      bar.volume(),
      bar.count(),
      bar.wap())
  }

  override def historicalDataEnd(reqId: Int,
                                 startDateStr: String,
                                 endDateStr: String): Unit = LogTry {
    historicalDataSink.finishHistoricalData(reqId,
      twsClock.millis(),
      toTimeMillis(startDateStr, statusListener.twsZoneId), // TODO
      toTimeMillis(endDateStr, statusListener.twsZoneId)) // TODO
  }
}

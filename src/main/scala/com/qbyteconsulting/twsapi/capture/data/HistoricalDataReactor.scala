package com.qbyteconsulting.twsapi.capture.data

import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.TimeZone

import com.ib.client.ContractDetails
import com.qbyteconsulting.reactor
import com.qbyteconsulting.reactor.{Reactor, ReactorCore}
import com.qbyteconsulting.twsapi.capture.config.{
  ContractConfig,
  HistoricalData
}
import com.qbyteconsulting.twsapi.capture.ib.ContractDb.ConId
import com.qbyteconsulting.twsapi.capture.ib.{
  ContractDb,
  ContractDetailsLoaded,
  ContractsConfigured
}
import org.influxdb.InfluxDB
import org.influxdb.dto.Query

import scala.collection.concurrent.TrieMap

object HistoricalDataReactor {

  val HeadDateFmt = "yyyyMMdd"

  def extractZoneId(twsConnectionTime: String, zone: ZoneId) = {
    val fmt = new SimpleDateFormat(HeadDateFmt)
    fmt.setTimeZone(TimeZone.getTimeZone(zone))
    fmt.parse(twsConnectionTime)
  }
}

class HistoricalDataReactor(val contractDb: ContractDb,
                            val db: InfluxDB,
                            val reactorCore: ReactorCore)
    extends Reactor {

  private val contractConfigsCache = new TrieMap[ConId, ContractConfig]()

  override def onEvent(event: reactor.ReactorEvent): Unit = {
    event match {
      case ContractsConfigured(configs) =>
        configs
          .filter(cfg => cfg.historicalData.isDefined)
          .foreach(cfg => contractConfigsCache.update(cfg.conid, cfg))
      case ContractDetailsLoaded(contractDetails) =>
        contractConfigsCache.get(contractDetails.conid()).map { cfg =>
          backfill(contractDetails, cfg.historicalData.get)
        }
      case _ => Unit
    }
  }

  private def backfill(contractDetails: ContractDetails,
                       historicalData: HistoricalData) = {
    println(
      s"backfill ${contractDetails.conid()} '${contractDetails.longName()}' $historicalData")
    val contract = contractDetails.contract()
    val timeZone = TimeZone.getTimeZone(contractDetails.timeZoneId())
    // TODO
  }

  private def selectMissingPeriods(instants: Long,
                                   conid: ConId,
                                   barSize: String): Seq[Long] = {
    val query =
      new Query(s"SELECT idle FROM $conid WHERE type = $barSize", "ibmktdata")
    //    db.query(query)
    Seq.empty[Long]
  }
}

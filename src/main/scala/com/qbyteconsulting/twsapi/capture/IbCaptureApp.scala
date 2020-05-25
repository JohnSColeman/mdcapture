package com.qbyteconsulting.twsapi.capture

import com.qbyteconsulting.twsapi.capture.config.ConfiguratorContractDbReactor
import com.qbyteconsulting.twsapi.capture.data._
import com.qbyteconsulting.twsapi.capture.ib.esocket.EClientSocketReactor
import com.qbyteconsulting.twsapi.capture.ib.ewrapper.ListeningEWrapper
import com.qbyteconsulting.twsapi.capture.ib.schedule.TradingScheduleReactor
import com.qbyteconsulting.twsapi.capture.ib.{IbHostParams, StateReactor}
import com.qbyteconsulting.reactor.ReactorCore

object IbCaptureApp extends App {

  val hostParams = IbHostParams(port = 7497)
  val db = data.getInfluxDb()

  val appReactorCore = new ReactorCore("Application Core", 32)
  val tickDataReactorCore = new ReactorCore("Data Core", 2048)
  val historicalDataReactorCore = new ReactorCore("Historical Data Core", 2048)

  val contractDbReactor = new ConfiguratorContractDbReactor(appReactorCore)
  val stateReactor = new StateReactor(appReactorCore)
  val scheduleReactor = new TradingScheduleReactor(appReactorCore)
  val historicalDataReactor =
    new HistoricalDataReactor(contractDbReactor, db, appReactorCore)
  val tickDataSink: TickDataSink = new ReactorTickDataSink(tickDataReactorCore)
  val historicalDataSink: HistoricalDataSink =
    new ReactorHistoricalDataSink(historicalDataReactorCore)
  val wrapper =
    new ListeningEWrapper(contractDbReactor,
                          stateReactor,
                          tickDataSink,
                          historicalDataSink)
  val requesterReactor =
    new EClientSocketReactor(hostParams, wrapper, appReactorCore)

  tickDataReactorCore.launch(new InfluxTickDataEventHandler(db)) // or LoggingTickDataEventHandler()
  val influxHistoricalDataEventHandler = new InfluxHistoricalDataEventHandler(
    db)
  appReactorCore.launch(stateReactor,
                        scheduleReactor,
                        contractDbReactor,
                        requesterReactor,
                        historicalDataReactor,
                        influxHistoricalDataEventHandler)
  historicalDataReactorCore.launch(influxHistoricalDataEventHandler)
}

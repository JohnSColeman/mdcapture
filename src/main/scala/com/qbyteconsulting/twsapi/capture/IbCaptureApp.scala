package com.qbyteconsulting.twsapi.capture

import com.qbyteconsulting.twsapi.capture.config.ConfiguratorContractDbReactor
import com.qbyteconsulting.twsapi.capture.data.{
  LoggingTickDataEventHandler,
  ReactorTickDataSink
}
import com.qbyteconsulting.twsapi.capture.ib.esocket.EClientSocketReactor
import com.qbyteconsulting.twsapi.capture.ib.ewrapper.ListeningEWrapperable
import com.qbyteconsulting.twsapi.capture.ib.schedule.TradingScheduleReactor
import com.qbyteconsulting.twsapi.capture.ib.{IbHostParams, StateReactor}
import com.qbyteconsulting.twsapi.capture.reactor.ReactorCore

object IbCaptureApp extends App {

  val hostParams = IbHostParams(port = 4002)

  val appReactorCore = new ReactorCore("Application Core", 32)
  val tickDataReactorCore = new ReactorCore("Data Core", 2048)

  val contractDbReactor = new ConfiguratorContractDbReactor(appReactorCore)
  val stateReactor = new StateReactor(appReactorCore)
  val scheduleReactor = new TradingScheduleReactor(appReactorCore)
  val tickDatSink: TickDataSink = new ReactorTickDataSink(tickDataReactorCore)
  val wrapper =
    new ListeningEWrapperable(contractDbReactor, stateReactor, tickDatSink)
  val requesterReactor =
    new EClientSocketReactor(hostParams, wrapper, appReactorCore)

  //tickDataReactorCore.launch(new InfluxTickDataEventHandler(data.getInfluxDb()))
  tickDataReactorCore.launch(new LoggingTickDataEventHandler())
  appReactorCore.launch(contractDbReactor,
                        stateReactor,
                        scheduleReactor,
                        requesterReactor)
}

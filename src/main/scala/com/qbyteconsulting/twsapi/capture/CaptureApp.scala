package com.qbyteconsulting.twsapi.capture

import com.qbyteconsulting.twsapi.capture.config.ConfiguratorContractDbReactor
import com.qbyteconsulting.twsapi.capture.data.{
  LoggingTickDataEventHandler,
  ReactorTickDataSink
}
import com.qbyteconsulting.twsapi.capture.ib.esocket.EClientSocketReactor
import com.qbyteconsulting.twsapi.capture.ib.ewrapper.ListeningEWrapperable
import com.qbyteconsulting.twsapi.capture.ib.{IbHostParams, StateReactor}
import com.qbyteconsulting.twsapi.capture.reactor.ReactorCore

object CaptureApp extends App {

  val hostParams = IbHostParams(port = 4002)

  val appReactorCore = new ReactorCore(16)
  val tickDataReactorCore = new ReactorCore(128)

  val contractDbReactor = new ConfiguratorContractDbReactor(appReactorCore)
  val stateReactor = new StateReactor(appReactorCore)
  val tickDatSink: TickDataSink = new ReactorTickDataSink(tickDataReactorCore)
  val wrapper =
    new ListeningEWrapperable(contractDbReactor, stateReactor, tickDatSink)
  val requesterReactor =
    new EClientSocketReactor(hostParams, wrapper, appReactorCore)

  tickDataReactorCore.launch(new LoggingTickDataEventHandler())
  appReactorCore.launch(contractDbReactor, stateReactor, requesterReactor)
}

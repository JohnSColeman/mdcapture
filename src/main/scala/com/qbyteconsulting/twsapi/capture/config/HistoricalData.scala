package com.qbyteconsulting.twsapi.capture.config

import com.ib.client.Types.BarSize

case class HistoricalData(barSize: BarSize, headDate: String, rth: Boolean = false)

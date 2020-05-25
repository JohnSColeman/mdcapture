package com.qbyteconsulting.twsapi.capture

import com.ib.client.Types.BarSize
import com.typesafe.config.ConfigFactory
import org.influxdb.{BatchOptions, InfluxDB, InfluxDBFactory}

package object data {

  val ThirtyMinutesMs = 30 * 60 * 1000

  val IdbLineFmt = "%d,type=%s val=%f %d"

  def getInfluxDb(): InfluxDB = {
    val config = ConfigFactory.load()
    val url = config.getString("influxdb.url")
    val datbaseName = config.getString("influxdb.databaseName")
    val username = config.getString("influxdb.username")
    val password = config.getString("influxdb.password")
    getInfluxDb(url, datbaseName, username, password)
  }

  def getInfluxDb(url: String = "http://127.0.0.1:8086",
                  databaseName: String,
                  username: String,
                  password: String): InfluxDB = {
    val db = InfluxDBFactory.connect(url, username, password)
    db.setDatabase(databaseName)
    db.enableBatch(BatchOptions.DEFAULTS.flushDuration(500))
    db
  }
}

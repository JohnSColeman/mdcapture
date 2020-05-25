# TwsApiCapture
A Scala client for the Interactive Brokers Trader Workstation API TWS-API

Features:
- robust recovery of broken connections
- influxdb time series database to store tick data
- architecture built on lmax disruptor
- stable thread management
- JMX dashboard

Maven build (not SBT), non-idiomatic OO style Scala. Backfill from historical data feature not complete.

## Prerequisites
JRE 1.8
Interactive Brokers trading account.
Download and install [TWS API](https://interactivebrokers.github.io/#)
Setup and rnning [influxdb](https://www.influxdata.com/)





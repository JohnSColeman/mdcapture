# TwsApiCapture
A Scala client for the Interactive Brokers Trader Workstation API TWS-API

Features:
- robust recovery of broken connections
- influxdb time series database to store tick data
- architecture built on lmax disruptor (not Scala Future)
- stable thread management
- JMX dashboard

Maven build (not SBT), non-idiomatic OO style Scala. Backfill from historical data feature not complete.

## Prerequisites
- JRE 1.8
- Interactive Brokers trading account.
- Download and install [TWS API](https://interactivebrokers.github.io/#)
- Setup and running [influxdb](https://www.influxdata.com/)

## Instructions
1. Install and set up prerequisites
2. Configure markets in application.conf
3. Run the project/application class IbCaptureApp




package com.qbyteconsulting.reactor

trait EventPublisher {

  def publish(event: ReactorEvent): Unit
}

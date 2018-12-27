package com.qbyteconsulting.twsapi.capture.jmx

import java.util.Collections
import java.util.concurrent.{CopyOnWriteArrayList, Executor}

import javax.management.{MBeanNotificationInfo, _}

trait NotificationBroadcaster extends NotificationEmitter {
  val notifInfo: Array[MBeanNotificationInfo]
  val notifExecutor: Executor

  private val listenerList = new CopyOnWriteArrayList[ListenerInfo]

  override def removeNotificationListener(listener: NotificationListener,
                                          filter: NotificationFilter,
                                          handback: Any): Unit = {

    val wildcard = new WildcardListenerInfo(listener)
    val removed =
      listenerList.removeAll(Collections.singleton(wildcard))
    if (!removed)
      throw new ListenerNotFoundException("Listener not registered")
  }

  override def addNotificationListener(listener: NotificationListener,
                                       filter: NotificationFilter,
                                       handback: Any): Unit = {
    if (listener == null) {
      throw new IllegalArgumentException("Listener can't be null")
    }

    listenerList.add(new ListenerInfo(listener, filter, handback))
  }

  override def removeNotificationListener(
      listener: NotificationListener): Unit = {
    val wildcard = new WildcardListenerInfo(listener);
    val removed =
      listenerList.removeAll(Collections.singleton(wildcard));
    if (!removed)
      throw new ListenerNotFoundException("Listener not registered");
  }

  override def getNotificationInfo: Array[MBeanNotificationInfo] = {
    if (notifInfo.length == 0) return notifInfo;
    else return notifInfo.clone();
  }

  def sendNotification(notification: Notification): Unit = {
    if (notification != null) {
      var enabled = false
      listenerList.forEach { li =>
        try {
          enabled = li.filter == null ||
            li.filter.isNotificationEnabled(notification)
        } catch {
          case e: Exception => Unit
        }
        if (enabled) {
          notifExecutor.execute(new SendNotifJob(notification, li))
        }
      }
    }
  }
}

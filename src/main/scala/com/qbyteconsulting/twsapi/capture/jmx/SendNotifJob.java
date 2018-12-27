package com.qbyteconsulting.twsapi.capture.jmx;

import javax.management.Notification;

public class SendNotifJob implements Runnable {

    private final Notification notif;
    private final ListenerInfo listenerInfo;

    public SendNotifJob(Notification notif, ListenerInfo listenerInfo) {
        this.notif = notif;
        this.listenerInfo = listenerInfo;
    }

    public void run() {
        try {
            listenerInfo.listener.handleNotification(notif, listenerInfo.handback);
        } catch (Exception e) {

        }
    }
}

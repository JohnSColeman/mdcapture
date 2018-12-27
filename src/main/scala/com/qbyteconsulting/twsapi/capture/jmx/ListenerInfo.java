package com.qbyteconsulting.twsapi.capture.jmx;

import javax.management.NotificationFilter;
import javax.management.NotificationListener;

public class ListenerInfo {
    NotificationListener listener;
    NotificationFilter filter;
    Object handback;

    ListenerInfo(NotificationListener listener,
                 NotificationFilter filter,
                 Object handback) {
        this.listener = listener;
        this.filter = filter;
        this.handback = handback;
    }

    public boolean equals(Object o) {
        if (!(o instanceof ListenerInfo))
            return false;
        ListenerInfo li = (ListenerInfo) o;
        if (li instanceof WildcardListenerInfo)
            return (li.listener == listener);
        else
            return (li.listener == listener && li.filter == filter
                    && li.handback == handback);
    }
}
package com.qbyteconsulting.twsapi.capture.jmx;

import javax.management.NotificationListener;

public class WildcardListenerInfo extends ListenerInfo {

    WildcardListenerInfo(NotificationListener listener) {
        super(listener, null, null);
    }

    public boolean equals(Object o) {
        assert (!(o instanceof WildcardListenerInfo));
        return o.equals(this);
    }
}

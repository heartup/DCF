package io.reactivej.dcf.common.protocol.worker;

import com.google.common.base.MoreObjects;

import java.io.Serializable;

public class RegisterReceived implements Serializable {
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .toString();
    }
}
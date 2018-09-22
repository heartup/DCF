package io.reactivej.dcf.common.protocol.acker;

import io.reactivej.dcf.common.info.AckerInfo;

import java.io.Serializable;

/***
 * @author wylihuanhuan1
 */
public class AckerInfoUpdated implements Serializable {

    private final AckerInfo info;

    public AckerInfoUpdated(AckerInfo info) {
        this.info = info;
    }

    public AckerInfo getInfo() {
        return info;
    }

    @Override
    public String toString() {
        return "AckerInfoUpdated{" +
                "info=" + info +
                '}';
    }
}

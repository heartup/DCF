package io.reactivej.dcf.common.protocol.leader;

import com.google.common.base.MoreObjects;
import io.reactivej.Failure;

import java.io.Serializable;

/***
 * @author heartup@gmail.com
 */
public class FailMessage implements Serializable {

    private final byte[] failure;

    public FailMessage(byte[] failure) {
        this.failure = failure;
    }

    public byte[] getFailure() {
        return failure;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("failure", "...")
                .toString();
    }
}

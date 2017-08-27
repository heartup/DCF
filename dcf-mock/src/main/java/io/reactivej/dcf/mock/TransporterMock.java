package io.reactivej.dcf.mock;

import io.reactivej.AbstractTransporter;
import io.reactivej.Envelope;
import io.reactivej.ReactiveSystem;
import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;

/***
 * @author heartup@gmail.com
 */
public class TransporterMock extends AbstractTransporter {

    public TransporterMock(ReactiveSystem system) {
        super(system);
    }

    @Override
    public void send(String host, int port, Envelope envlop) {
        Envelope newEnvelop = transportLocally(envlop);
        ((ReactiveSystemMock)getSystem()).getCluster().getSystems().get(port).getTransporter().receive(newEnvelop, null);
    }

    @Override
    public void suspendReadingMessage() {

    }

    @Override
    public void resumeReadingMessage() {

    }

    private Envelope transportLocally(Envelope envlop) {
        return new Envelope(envlop.getReceiver(), (Serializable) SerializationUtils.clone(envlop.getMessage()), envlop.getSender());
    }
}

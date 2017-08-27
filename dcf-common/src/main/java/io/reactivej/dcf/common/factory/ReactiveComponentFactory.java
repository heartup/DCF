package io.reactivej.dcf.common.factory;

import io.reactivej.ReactiveRef;
import io.reactivej.ReactiveSystem;

/**
 * @author heartup@gmail.com on 4/11/16.
 */
public class ReactiveComponentFactory {

    public static ReactiveRef createComponent(ReactiveSystem system, String name) {
        return system.createReactiveComponent(name);
    }
}

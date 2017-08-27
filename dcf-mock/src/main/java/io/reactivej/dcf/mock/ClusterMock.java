package io.reactivej.dcf.mock;

import io.reactivej.AbstractTransporter;
import io.reactivej.ReactiveSystem;

import java.util.concurrent.ConcurrentHashMap;

/***
 * @author heartup@gmail.com
 */
public class ClusterMock {

    private String id;

    private ConcurrentHashMap<String, Integer> singletons = new ConcurrentHashMap<>();

    private ConcurrentHashMap<Integer, ReactiveSystem> systems = new ConcurrentHashMap<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ConcurrentHashMap<String, Integer> getSingletons() {
        return singletons;
    }

    public ConcurrentHashMap<Integer, ReactiveSystem> getSystems() {
        return systems;
    }
}

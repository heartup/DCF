package io.reactivej.dcf.common.protocol.worker;

import com.google.common.base.MoreObjects;
import io.reactivej.dcf.common.info.TopologySchedule;

import java.io.Serializable;

public class TopologyScheduleChanged implements Serializable {

    private TopologySchedule schedule;

    public TopologyScheduleChanged(TopologySchedule schedule) {
        this.schedule = schedule;
    }

    public TopologySchedule getSchedule() {
        return schedule;
    }

    public void setSchedule(TopologySchedule schedule) {
        this.schedule = schedule;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("schedule", schedule)
                .toString();
    }
}
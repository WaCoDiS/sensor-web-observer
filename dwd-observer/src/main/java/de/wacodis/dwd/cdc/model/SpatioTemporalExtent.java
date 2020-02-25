package de.wacodis.dwd.cdc.model;

import java.util.ArrayList;

import org.joda.time.DateTime;

/**
 * Encapsulates the spatio-temporal extent of DWD products
 */
public class SpatioTemporalExtent {
    private ArrayList<DateTime> timeFrame;
    private Envelope envelope;

    public ArrayList<DateTime> getTimeFrame() {
        return timeFrame;
    }

    public void setTimeFrame(ArrayList<DateTime> timeFrame) {
        this.timeFrame = timeFrame;
    }

    public Envelope getbBox() {
        return envelope;
    }

    public void setbBox(Envelope envelope) {
        this.envelope = envelope;
    }
}

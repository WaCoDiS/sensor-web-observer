package de.wacodis.dwd.cdc;

import java.util.ArrayList;

import org.joda.time.DateTime;

public class SpatioTemporalExtent {
    private ArrayList<DateTime> timeFrame;
    private ArrayList<Float> bBox;


    public ArrayList<DateTime> getTimeFrame() {
        return timeFrame;
    }

    public void setTimeFrame(ArrayList<DateTime> timeFrame) {
        this.timeFrame = timeFrame;
    }

    public ArrayList<Float> getbBox() {
        return bBox;
    }

    public void setbBox(ArrayList<Float> bBox) {
        this.bBox = bBox;
    }


}

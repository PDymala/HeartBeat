package com.example.heartbeat;

public class SingleBeat {

 long singleBeatTime;
 long averageBeatTime;

    SingleBeat(long singleBeatTime){
    this.singleBeatTime = singleBeatTime;
    }

    public void setAverageBeatTime(long averageBeatTime) {
        this.averageBeatTime = averageBeatTime;
    }

    long getDistanceToAverage(){
        return Math.abs(averageBeatTime - singleBeatTime);
    }

    public long getSingleBeatTime() {
        return singleBeatTime;
    }

    @Override
    public String toString() {
        return "SingleBeat{" +
                "singleBeatTime=" + singleBeatTime +
                ", dist to av=" + getDistanceToAverage() +
                '}';
    }
}

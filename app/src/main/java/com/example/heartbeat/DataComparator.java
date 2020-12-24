package com.example.heartbeat;


import java.util.Comparator;
//api 24, replaced with api 16 collections
public class DataComparator implements Comparator<SingleBeat> {

        public int compare(SingleBeat e1, SingleBeat e2) {
            if(e1.getDistanceToAverage() > e2.getDistanceToAverage()){
                return 1;
            } else {
                return -1;
            }
        }
    }



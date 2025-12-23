package com.antmendoza;

import java.lang.reflect.Constructor;

public class MySignalRequest {

    private String signal;
    private Boolean signalSpecificFlag;

    private Integer firstCounter;

    private Integer secondCounter;


    public MySignalRequest(){}

    public MySignalRequest(final String signal,  final Integer firstCounter, final Integer secondCounter) {
        this.signal = signal;
        this.firstCounter = firstCounter;
        this.secondCounter = secondCounter;
    }

    public Boolean getSignalSpecificFlag() {
        return signalSpecificFlag != null ? signalSpecificFlag :  firstCounter != 0;
    }




    public String getSignal() {
        return signal;
    }

    public void setSignal(final String signal) {
        this.signal = signal;
    }


    public void setSignalSpecificFlag(final Boolean signalSpecificFlag) {
        this.signalSpecificFlag = signalSpecificFlag;
    }

    public Integer getFirstCounter() {
        return firstCounter;
    }

    public void setFirstCounter(final Integer firstCounter) {
        this.firstCounter = firstCounter;
    }

    public Integer getSecondCounter() {
        return secondCounter;
    }

    public void setSecondCounter(final Integer secondCounter) {
        this.secondCounter = secondCounter;
    }
}

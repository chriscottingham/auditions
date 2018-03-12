package com.ef.domain;

import java.time.Duration;
import java.time.temporal.TemporalAmount;

public enum LogDuration {
    HOURLY("hourly", Duration.ofHours(1)),
    DAILY("daily", Duration.ofDays(1));
    
    public static LogDuration forValue(String value) {
        
        LogDuration returnLogDuration = null;
        for (LogDuration logDuration : values()) {
            if (logDuration.getArgString().equals(value)) {
                returnLogDuration = logDuration;
                break;
            }
        }
        return returnLogDuration;
    }
    
    private final String argString;
    private final Duration duration;
    
    private LogDuration(String argString, Duration duration) {
        
        this.argString = argString;
        this.duration = duration;
    }
    
    public String getArgString() {
        return argString;
    }
    
    public TemporalAmount getTemporalAmount() {
        return duration;
    }
}

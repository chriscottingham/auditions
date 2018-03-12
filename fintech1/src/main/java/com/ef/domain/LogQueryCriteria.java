package com.ef.domain;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class LogQueryCriteria implements ValueListable {
    
    //Putting persistence information here is not ideal.  An ORM or mapping layer would be cleaner, so persistence info doesn't pollute domain objects, but I
    // only have two entities...
    public static List<String> getColumnNames() {
        return Arrays.asList(new String[]{"start_date", "log_duration", "threshold"});
    }
    
    private LocalDateTime startDate;
    private LogDuration logDuration;
    private int threshold;
    private String logFile;
    
    public LogQueryCriteria() {
    
    }
    
    public LogQueryCriteria(LocalDateTime startDate, LogDuration logDuration, int threshold, String logFile) {
        this.startDate = startDate;
        this.logDuration = logDuration;
        this.threshold = threshold;
        this.logFile = logFile;
    }
    
    public LogQueryCriteria(LocalDateTime startDate, LogDuration logDuration, int threshold) {
        this.startDate = startDate;
        this.logDuration = logDuration;
        this.threshold = threshold;
    }
    
    public LogDuration getLogDuration() {
        return logDuration;
    }
    
    public String getLogFile() {
        return logFile;
    }
    
    public String getPrettyPrint() {
        return "startDate: " + startDate + ", duration: " + logDuration + ", threshold: " + threshold;
    }
    
    public LocalDateTime getStartDate() {
        return startDate;
    }
    
    public int getThreshold() {
        return threshold;
    }
    
    public List<Object> getValues() {
        return Arrays.asList(new Object[]{startDate, logDuration, threshold});
    }
    
    public boolean isApplicable(LogEntry entry) {
        return entry.getEntryTime().isBefore(startDate.plus(logDuration.getTemporalAmount()));
    }
    
    public void setLogDuration(LogDuration logDuration) {
        this.logDuration = logDuration;
    }
    
    public void setLogFile(String logFile) {
        this.logFile = logFile;
    }
    
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }
    
    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }
}

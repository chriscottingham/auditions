package com.ef.domain;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class LogEntry implements ValueListable {
    
    //Putting persistence information here is not ideal.  An ORM or mapping layer would be cleaner, so persistence info doesn't pollute domain objects, but I
    // only have two entities...
    public static List<String> getColumnNames() {
        return Arrays.asList(new String[]{"entry_time", "ip_address", "request", "http_status", "user_agent"});
    }
    
    private LocalDateTime entryTime;
    private String request;
    private int httpStatus;
    private String ipAddress;
    private String userAgent;
    
    public LocalDateTime getEntryTime() {
        return entryTime;
    }
    
    public int getHttpStatus() {
        return httpStatus;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public String getRequest() {
        return request;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public List<Object> getValues() {
        return Arrays.asList(new Object[]{entryTime, ipAddress, request, httpStatus, userAgent});
    }
    
    public void setEntryTime(LocalDateTime entryTime) {
        this.entryTime = entryTime;
    }
    
    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public void setRequest(String request) {
        this.request = request;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}

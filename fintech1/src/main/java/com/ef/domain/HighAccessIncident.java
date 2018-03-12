package com.ef.domain;

import java.util.ArrayList;
import java.util.List;

public class HighAccessIncident implements ValueListable {
    
    //Putting persistence information here is not ideal.  An ORM or mapping layer would be cleaner, so persistence info doesn't pollute domain objects, but I
    // only have two entities...
    public static List<String> getColumnNames() {
        ArrayList<String> names = new ArrayList<>();
        names.add("ip_address");
        names.add("occurrence_count");
        names.addAll(LogQueryCriteria.getColumnNames());
        return names;
    }
    
    private String ipAddress;
    private int occurrenceCount;
    private LogQueryCriteria matchCriteria;
    
    public HighAccessIncident() {
    }
    
    public HighAccessIncident(String ipAddress, int occurrenceCount, LogQueryCriteria matchCriteria) {
        this.ipAddress = ipAddress;
        this.occurrenceCount = occurrenceCount;
        this.matchCriteria = matchCriteria;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public LogQueryCriteria getMatchCriteria() {
        return matchCriteria;
    }
    
    public int getOccurrenceCount() {
        return occurrenceCount;
    }
    
    public String getPrettyPrint() {
        return "ip: " + ipAddress + ", count: " + occurrenceCount + ", " + matchCriteria.getPrettyPrint();
    }
    
    public List<Object> getValues() {
        ArrayList<Object> values = new ArrayList<>();
        values.add(ipAddress);
        values.add(occurrenceCount);
        values.addAll(matchCriteria.getValues());
        return values;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public void setMatchCriteria(LogQueryCriteria matchCriteria) {
        this.matchCriteria = matchCriteria;
    }
    
    public void setOccurrenceCount(int occurrenceCount) {
        this.occurrenceCount = occurrenceCount;
    }
}

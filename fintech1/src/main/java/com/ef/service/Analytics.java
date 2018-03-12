package com.ef.service;

import com.ef.domain.HighAccessIncident;
import com.ef.domain.LogEntry;
import com.ef.domain.LogQueryCriteria;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Analytics implements LogEntryListener {
    
    private final LogQueryCriteria logQueryCriteria;
    
    private HashMap<String, Integer> periodIpAccesses = new HashMap<>();
    
    public Analytics(LogQueryCriteria logQueryCriteria) {
        this.logQueryCriteria = logQueryCriteria;
    }
    
    public List<HighAccessIncident> getHighAccessIncidents() {
        
        List<HighAccessIncident> incidents = new ArrayList<>();
        
        for (Map.Entry<String, Integer> entry : periodIpAccesses.entrySet()) {
            if (entry.getValue() >= logQueryCriteria.getThreshold()) {
                incidents.add(new HighAccessIncident(entry.getKey(), entry.getValue(), logQueryCriteria));
            }
        }
        
        return incidents;
    }
    
    @Override
    public void logEntryRead(LogEntry entry) {
        
        if (logQueryCriteria.isApplicable(entry)) {
            Integer count = periodIpAccesses.get(entry.getIpAddress());
            if (count == null) {
                count = 0;
            }
            periodIpAccesses.put(entry.getIpAddress(), count + 1);
        }
    }
}

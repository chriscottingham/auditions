package com.ef;

import com.ef.domain.HighAccessIncident;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class TestUtil {
    
    public static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd.HH:mm:ss");
    
    public static boolean containsIp(List<HighAccessIncident> incidents, String testIp) {
        
        boolean contains = false;
        
        for (HighAccessIncident incident : incidents) {
            if (incident.getIpAddress().equals(testIp)) {
                contains = true;
                break;
            }
        }
        return contains;
    }
}

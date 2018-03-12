package com.ef;

import com.ef.domain.LogDuration;
import com.ef.domain.LogQueryCriteria;
import com.ef.service.Analytics;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDateTime;

public class InMenoryOccurrenceTest {
    
    @Test
    public void testDailyCount() throws IOException {
        
        Analytics analytics = new Analytics(new LogQueryCriteria(LocalDateTime.parse("2017-01-01.00:00:00", TestUtil.dateFormat), LogDuration.DAILY, 500));
        
        Parser parser = new Parser();
        parser.addLogEntryListener(analytics);
        
        parser.parse(Parser.ACCESS_LOG_FILE);
        
        String testIp = "192.168.102.136";
        Assert.assertTrue("Test ip " + testIp + "  not contained in matched ip addresses", TestUtil.containsIp(analytics.getHighAccessIncidents(), testIp));
    }
    
    @Test
    public void testHourlyCount() throws IOException {
        
        Analytics analytics = new Analytics(new LogQueryCriteria(LocalDateTime.parse("2017-01-01.15:00:00", TestUtil.dateFormat), LogDuration.HOURLY, 200));
        
        Parser parser = new Parser();
        parser.addLogEntryListener(analytics);
        
        parser.parse(Parser.ACCESS_LOG_FILE);
        
        String testIp = "192.168.11.231";
        Assert.assertTrue("Test ip " + testIp + " not contained in matched ip addresses", TestUtil.containsIp(analytics.getHighAccessIncidents(), testIp));
    }
    
}

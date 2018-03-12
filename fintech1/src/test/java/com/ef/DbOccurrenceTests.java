package com.ef;

import com.ef.domain.HighAccessIncident;
import com.ef.domain.LogDuration;
import com.ef.domain.LogQueryCriteria;
import com.ef.service.EfException;
import com.ef.service.PersistenceManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class DbOccurrenceTests {
    
    private PersistenceManager persistence = new PersistenceManager();
    
    @After
    public void closeDb() throws EfException {
        persistence.closeConnection();
    }
    
    @Before
    public void populateDb() throws EfException, FileNotFoundException {
        
        persistence.openConnection();
        
        Parser parser = new Parser();
        parser.addLogEntryListener(persistence);
        
        //Making a behavioral assumption here: deleting real rows during a test.  I'm assuming that, each time the tool is run, the rows will be deleted
        // anyway, so deleting them here, in the test, is a consistent behavior.
        persistence.clearTables();
        
        parser.parse(Parser.ACCESS_LOG_FILE);
    }
    
    @Test
    public void testHourlyIncidents() throws SQLException {
        
        List<HighAccessIncident> incidents = persistence.getHighAccessIpAddresses(new LogQueryCriteria(LocalDateTime.parse("2017-01-01.15:00:00", TestUtil
                .dateFormat), LogDuration.HOURLY, 200));
        
        String testIp = "192.168.11.231";
        Assert.assertTrue("Test ip " + testIp + "  not contained in matched ip addresses", TestUtil.containsIp(incidents, testIp));
    }
    
    @Test
    public void testDailyIncidents() throws SQLException {
        
        List<HighAccessIncident> incidents = persistence.getHighAccessIpAddresses(new LogQueryCriteria(LocalDateTime.parse("2017-01-01.00:00:00", TestUtil
                .dateFormat), LogDuration.DAILY, 500));
        
        String testIp = "192.168.102.136";
        Assert.assertTrue("Test ip " + testIp + " not contained in matched ip addresses", TestUtil.containsIp(incidents, testIp));
        
    }
    
}

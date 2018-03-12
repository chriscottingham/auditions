package com.ef;

import com.ef.domain.HighAccessIncident;
import com.ef.domain.LogQueryCriteria;
import com.ef.service.Analytics;
import com.ef.service.EfException;
import com.ef.service.PersistenceManager;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Heart of the application:
 *  parse the commandline,
 *  wire up listeners for access log pojo line items,
 *  start parsing and generating events
 */
public class AccessLogAnalyzer {
    
    public static String ACCESS_LOG_FILE = "src/main/resources/access.log";
    
    public static void logIncidentsToConsole(List<HighAccessIncident> incidents) {
        
        System.out.println("Found " + incidents.size() + " matching ip addresses:");
        System.out.println(HighAccessIncident.getColumnNames());
        
        //May as well give it some kind of order for anyone looking
        Collections.sort(incidents, new Comparator<HighAccessIncident>() {
            @Override
            public int compare(HighAccessIncident o1, HighAccessIncident o2) {
                return Integer.valueOf(o1.getOccurrenceCount()).compareTo(Integer.valueOf(o2.getOccurrenceCount()));
            }
        });
        
        //Could be nice to fixed-width pretty-print this, but it wasn't in spec, to avoiding gold-plating
        for (HighAccessIncident incident : incidents) {
            System.out.println(incident.getValues());
        }
    }
    
    private final LogQueryCriteria queryCriteria;
    private final Analytics analytics;
    private final PersistenceManager persistenceManager;
    
    public AccessLogAnalyzer(LogQueryCriteria logQueryCriteria) {
        
        this.queryCriteria = logQueryCriteria;
        
        analytics = new Analytics(queryCriteria);
        persistenceManager = new PersistenceManager();
    }
    
    public void runBatch() throws EfException {
        
        Parser parser = new Parser();
        parser.addLogEntryListener(persistenceManager);
        parser.addLogEntryListener(analytics);
        
        persistenceManager.openConnection();
        
/*
            Because each execution of the application will load access entries, I'm deleting what's there to prevent duplicates.  Another strategy, such as
            using datetimes as primary keys (if they were truly unique) or using some other method to determine uniqueness, and
            doing a sync between database and log file, is likely a better alternative than a delete and reinsert, as I'm doing here.
*/
        persistenceManager.clearTables();
        
        String accessLog = queryCriteria.getLogFile();
        if (accessLog == null) {
            accessLog = ACCESS_LOG_FILE;
        }
        parser.parse(accessLog);
        
        List<HighAccessIncident> incidents = analytics.getHighAccessIncidents();
        if (incidents.size() > 0) {
            logIncidentsToConsole(incidents);
            persistenceManager.save(incidents);
        }
        
        persistenceManager.closeConnection();
        
        
    }
}

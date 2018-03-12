package com.ef;

import com.ef.domain.LogEntry;
import com.ef.domain.LogQueryCriteria;
import com.ef.service.EfException;
import com.ef.service.LogEntryListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Parses log file and emits line pojo events
 */
public class Parser {
    
    private static final Logger logger = LoggerFactory.getLogger(Parser.class.getName());
    
    public static final DateTimeFormatter logDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    /**
     * The spec said that the application should be executed from com.ef.Parser.
     *
     * @param args
     * @throws EfException
     */
    public static final void main(String[] args) throws EfException {
        
        LogQueryCriteria logQueryCriteria = CommandLineHandler.parseArguments(args);
        new AccessLogAnalyzer(logQueryCriteria).runBatch();
    }
    
    private List<LogEntryListener> logEntryListeners = new ArrayList<>();
    
    protected void addLogEntryListener(LogEntryListener l) {
        logEntryListeners.add(l);
    }
    
    public void parse(String accessLogFile) throws EfException {
        
        Scanner scanner = null;
        try {
            scanner = new Scanner(new InputStreamReader(new BufferedInputStream(new FileInputStream(accessLogFile))));
            scanner.useDelimiter("[|\n\r]");
            
            int columnIndex = 0;
            int lineIndex = 0;
            LogEntry entry = new LogEntry();
            while (scanner.hasNext()) {
                
                String value = scanner.next();
                
                logger.trace("column: " + columnIndex + " : " + value);
                
                switch (columnIndex) {
                    case 0:
                        entry.setEntryTime(LocalDateTime.from(logDateFormat.parse(value)));
                        break;
                    case 1:
                        entry.setIpAddress(value);
                        break;
                    case 2:
                        entry.setRequest(value);
                        break;
                    case 3:
                        entry.setHttpStatus(Integer.parseInt(value));
                        break;
                    case 4:
                        entry.setUserAgent(value);
                        break;
                    default:
                        
                        for (LogEntryListener l : logEntryListeners) {
                            l.logEntryRead(entry);
                        }
                        entry = new LogEntry();
                        
                        if (lineIndex % 10000 == 0) {
                            System.out.println("Processed line " + lineIndex + " of log");
                        }
                        
                        columnIndex = -1;
                        lineIndex++;
                        
                        break;
                }
                
                columnIndex++;
            }
            
            scanner.close();
            
        } catch (FileNotFoundException e) {
            throw new EfException("Problem parsing access log", e);
        }
    }
    
    
}

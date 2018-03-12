package com.ef.service;

import com.ef.domain.HighAccessIncident;
import com.ef.domain.LogEntry;
import com.ef.domain.LogQueryCriteria;
import com.mysql.cj.jdbc.MysqlDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;

public class PersistenceManager implements LogEntryListener {
    
    public static final Logger logger = LoggerFactory.getLogger(PersistenceManager.class.getName());
    
    public static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss.SSS");
    
    public static final String DAILY_QUERY = "select * from (select ip_address, count(ip_address) ip_count from access_log where date(entry_time) >= ? and "
            + "date" + "(entry_time) < date_add(?, interval 1 day) group by ip_address) as counts where counts.ip_count > ? order by counts.ip_count";
    
    public static final String HOURLY_QUERY = "select * from (select ip_address, count(ip_address) ip_count from access_log where entry_time >= " + "? and "
            + "entry_time < date_add(?, interval 1 hour) group by ip_address) as counts where counts.ip_count > ? order by counts.ip_count";
    
    public static final String WALLETHUB_DB = "wallethub";
    
    public static final String LOG_ENTRY_TABLE_NAME = "access_log";
    public static final String HIGH_ACCESS_INCIDENT_TABLE_NAME = "high_access_incident";
    
    private MysqlDataSource dataSource;
    private Connection connection;
    
    private PreparedStatement highAccessPreparedStatement;
    private PreparedStatement accessLogPreparedStatement;
    
    public PersistenceManager() {
        
        dataSource = new MysqlDataSource();
        dataSource.setDatabaseName(WALLETHUB_DB);
        dataSource.setUser("root");
    }
    
    public void clearTables() throws EfException {
        try {
            connection.createStatement().execute("delete from " + LOG_ENTRY_TABLE_NAME);
            connection.createStatement().execute("delete from " + HIGH_ACCESS_INCIDENT_TABLE_NAME);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void closeConnection() throws EfException {
        try {
            accessLogPreparedStatement.close();
            highAccessPreparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            //Can I really do something meaningful with this information?
            e.printStackTrace();
        }
    }
    
    //So, this is ugly.  I'm doing old school jdbc serialization.  Obvious choice to avoid this is hibernate, but it's so big for my two entities...
    //Just as I reached the end of my willingness to fiddle with this, it just worked, so I guess here it is.
    private String formatValues(List<?> values, boolean quote) {
        StringBuilder builder = new StringBuilder();
        for (Object object : values) {
            if (quote) {
                builder.append("\"");
            }
            if (object instanceof LocalDateTime) {
                builder.append(dateFormat.format((TemporalAccessor) object));
            } else {
                builder.append(object.toString().replaceAll("\"", ""));
            }
            if (quote) {
                builder.append("\"");
            }
            builder.append(",");
        }
        builder.delete(builder.length() - 1, builder.length());
        return builder.toString();
    }
    
    
    public List<HighAccessIncident> getHighAccessIpAddresses(LogQueryCriteria criteria) throws SQLException {
        
        ArrayList<HighAccessIncident> incidents = new ArrayList<>();
        
        String query = "";
        switch (criteria.getLogDuration()) {
            case HOURLY:
                query = HOURLY_QUERY;
                break;
            case DAILY:
                query = DAILY_QUERY;
                break;
        }
        
        Timestamp timestamp = Timestamp.valueOf(criteria.getStartDate());
        
        //This could theoretically break with an impty query, but it _is_ a test...
        PreparedStatement prepared = connection.prepareStatement(query);
        prepared.setTimestamp(1, timestamp);
        prepared.setTimestamp(2, timestamp);
        prepared.setInt(3, criteria.getThreshold());
        
        ResultSet resultSet = prepared.executeQuery();
        while (resultSet.next()) {
            HighAccessIncident incident = new HighAccessIncident();
            incident.setIpAddress(resultSet.getString(1));
            incident.setOccurrenceCount(resultSet.getInt(2));
            incident.setMatchCriteria(criteria);
            incidents.add(incident);
        }
        
        return incidents;
    }
    
    @Override
    public void logEntryRead(LogEntry entry) {
        
        try {
            save(entry);
        } catch (EfException e) {
            logger.warn("Problem saving logEntry: " + entry.getEntryTime(), e);
        }
    }
    
    public void openConnection() throws EfException {
        try {
            connection = dataSource.getConnection();
            
            accessLogPreparedStatement = connection.prepareStatement("insert into " + LOG_ENTRY_TABLE_NAME + " (" + formatValues(LogEntry.getColumnNames(),
                    false) + ") values (?,?,?,?,?)");
            
            highAccessPreparedStatement = connection.prepareStatement("insert into " + HIGH_ACCESS_INCIDENT_TABLE_NAME + " (" + formatValues
                    (HighAccessIncident.getColumnNames(), false) + ") values " + "(?, ?, ?, ?, ?)");
            
        } catch (SQLException e) {
            throw new EfException("Failure connecting to DB");
        }
    }
    
    public void save(LogEntry entry) throws EfException {
        saveRow(accessLogPreparedStatement, entry.getValues());
    }
    
    public void save(List<HighAccessIncident> incidents) throws EfException {
        for (HighAccessIncident incident : incidents) {
            saveRow(highAccessPreparedStatement, incident.getValues());
        }
    }
    
    public void saveRow(PreparedStatement statement, List<Object> values) throws EfException {
        
        try {
            int parameterIndex = 1;
            
            for (Object value : values) {
                
                if (value instanceof LocalDateTime) {
                    statement.setTimestamp(parameterIndex, java.sql.Timestamp.valueOf((LocalDateTime) value));
                } else if (value instanceof Integer) {
                    statement.setInt(parameterIndex, (Integer) value);
                } else {
                    statement.setString(parameterIndex, value.toString());
                }
                parameterIndex++;
            }
            statement.execute();
            
        } catch (SQLException e) {
            throw new EfException("Failed to insert high access incident", e);
        }
    }
    
}

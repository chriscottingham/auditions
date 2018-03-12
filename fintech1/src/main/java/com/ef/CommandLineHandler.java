package com.ef;

import com.ef.domain.LogDuration;
import com.ef.domain.LogQueryCriteria;
import com.ef.service.EfException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CommandLineHandler {
    
    public static final DateTimeFormatter commandLineDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd.HH:mm:ss");
    
    public enum CommandLineArgument {
        START_DATE("s", "startDate", true, "Start Date: (yyyy-mm-dd.HH24:mm:ss"),
        DURATION("d", "duration", true, "How long a period to evaluate (hourly, daily)"),
        THRESHOLD("t", "threshold", true, "How many times must an ip appear to be displayed?"),
        LOG_FILE("a", "accesslog", false, "The file containing log entries?");
        
        private String shortName;
        private String longName;
        private boolean required;
        private String summary;
        
        CommandLineArgument(String shortName, String longName, boolean required, String summary) {
            this.shortName = shortName;
            this.longName = longName;
            this.required = required;
            this.summary = summary;
        }
        
        public static CommandLineArgument forLongName(String longOpt) {
            CommandLineArgument returnArgument = null;
            for (CommandLineArgument argument : values()) {
                if (argument.getLongName().equals(longOpt)) {
                    returnArgument = argument;
                    break;
                }
            }
            return returnArgument;
        }
        
        public String getShortName() {
            return shortName;
        }
        
        public String getLongName() {
            return longName;
        }
        
        public boolean isRequired() {
            return required;
        }
        
        public String getSummary() {
            return summary;
        }
    }
    
    private static final Logger logger = LoggerFactory.getLogger(CommandLineHandler.class.getName());
    
    private static final Options commandLineOptions = new Options();
    
    static {
        for (CommandLineArgument argument : CommandLineArgument.values()) {
            commandLineOptions.addRequiredOption(argument.getShortName(), argument.getLongName(), argument.isRequired(), argument.getShortName());
        }
    }
    
    public static LogQueryCriteria parseArguments(String[] args) throws EfException {
        
        LogQueryCriteria config = new LogQueryCriteria();
        
        CommandLine commandLine = null;
        try {
            commandLine = new DefaultParser().parse(commandLineOptions, args);
            
            for (Option option : commandLine.getOptions()) {
                
                //This switch statement inside the loop will end up being O(CommandLineArguments.length^2), but it runs rarely, with a small number of
                // arguments,
                // and using the enum provides type safety and protection against typos, so I think it's worth it
                logger.trace("Found command line option: " + option.getLongOpt() + " : " + commandLine.getOptionValue(option.getLongOpt()));
                switch (CommandLineArgument.forLongName(option.getLongOpt())) {
                    case START_DATE:
                        config.setStartDate(LocalDateTime.parse(option.getValue(), commandLineDateFormat));
                        break;
                    case DURATION:
                        config.setLogDuration(LogDuration.forValue(option.getValue()));
                        break;
                    case THRESHOLD:
                        config.setThreshold(Integer.parseInt(option.getValue()));
                        break;
                    case LOG_FILE:
                        config.setLogFile(option.getValue());
                        break;
                }
            }
        } catch (ParseException e) {
            throw new EfException("Problem parsing command line arguments", e);
        }
        return config;
    }
    
}

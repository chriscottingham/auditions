package com.ef.log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Generator {
    
    private static final Pattern USER_AGENT_PARSE_PATTERN = Pattern.compile("\r\n");
    private static final String USER_AGENTS_FILE = "userAgents.txt";
    
    //The handling of checked exceptions is a standard debate topic.  Here, I figure I'm only running this in development, and a failure should consitute a
    // crash, whereupon I can just fix the code - so I feel like just propagating my exception up to main is fine.
    public static final void main(String[] args) throws FileNotFoundException {
        
        new Generator(100, 100).generate("access_log.txt");
    }
    
    private int ipAddressCount;
    private int maxOccurrences;
    
    public Generator(int ipAddressCount, int maxOccurrences) throws FileNotFoundException {
        this.ipAddressCount = ipAddressCount;
        this.maxOccurrences = maxOccurrences;
        
        parseUserAgents();
    }
    
    protected void parseUserAgents() throws FileNotFoundException {
        
        Scanner scanner = new Scanner(new InputStreamReader(new FileInputStream(USER_AGENTS_FILE)));
        scanner.useDelimiter(USER_AGENT_PARSE_PATTERN);
        
    }
    
    public void generate(String fileName) {
    
    }
    
}

package com.ef.service;

import com.ef.domain.LogEntry;

public interface LogEntryListener {
    
    void logEntryRead(LogEntry entry);
}

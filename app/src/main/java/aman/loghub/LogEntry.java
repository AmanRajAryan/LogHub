package aman.loghub;

import java.io.Serializable;

public class LogEntry implements Serializable {
    
    public int id; // Managed manually by SQLite
    
    public String appName;
    public String tag;
    public String message;
    public String level; // DEBUG, INFO, WARN, ERROR
    public long timestamp;
    
    public LogEntry(String appName, String tag, String message, String level, long timestamp) {
        this.appName = appName;
        this.tag = tag;
        this.message = message;
        this.level = level;
        this.timestamp = timestamp;
    }
    
    // Empty constructor for flexibility
    public LogEntry() {}
}

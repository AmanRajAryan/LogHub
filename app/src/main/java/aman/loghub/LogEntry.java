package aman.loghub;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "logs")
public class LogEntry implements Serializable {
    
    @PrimaryKey(autoGenerate = true)
    public int id;
    
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
}
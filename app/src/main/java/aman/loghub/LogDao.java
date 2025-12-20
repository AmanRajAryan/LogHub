package aman.loghub;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface LogDao {
    
    @Insert
    void insert(LogEntry logEntry);
    
    @Query("SELECT * FROM logs ORDER BY timestamp ASC")
    List<LogEntry> getAllLogs();
    
    @Query("SELECT * FROM logs WHERE appName = :appName ORDER BY timestamp ASC")
    List<LogEntry> getLogsByApp(String appName);
    
    @Query("SELECT * FROM logs WHERE level = :level ORDER BY timestamp ASC")
    List<LogEntry> getLogsByLevel(String level);
    
    @Query("SELECT * FROM logs WHERE tag = 'CRASH' ORDER BY timestamp ASC")
    List<LogEntry> getCrashLogs();
    
    @Query("DELETE FROM logs")
    void deleteAll();
    
    @Query("SELECT DISTINCT appName FROM logs ORDER BY appName")
    List<String> getAllAppNames();
    
    @Query("SELECT COUNT(*) FROM logs")
    int getLogCount();
    
    @Query("SELECT COUNT(*) FROM logs WHERE level = :level")
    int getLogCountByLevel(String level);
    
    @Query("SELECT COUNT(*) FROM logs WHERE tag = 'CRASH'")
    int getCrashCount();
}
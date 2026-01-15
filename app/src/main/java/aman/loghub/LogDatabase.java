package aman.loghub;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class LogDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "loghub_database";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_LOGS = "logs";

    private static LogDatabase instance;

    public static synchronized LogDatabase getInstance(Context context) {
        if (instance == null) {
            instance = new LogDatabase(context.getApplicationContext());
        }
        return instance;
    }

    private LogDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LOGS_TABLE = "CREATE TABLE " + TABLE_LOGS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "appName TEXT, " +
                "tag TEXT, " +
                "message TEXT, " +
                "level TEXT, " +
                "timestamp INTEGER)";
        db.execSQL(CREATE_LOGS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGS);
        onCreate(db);
    }

    // --- DAO METHODS IMPLEMENTATION ---

    // NOTE: We removed '.logDao()' from the call chain.
    // So you will need to find-and-replace: 
    // ".logDao()."  ->  "."  (in your MainActivity and Provider)

    public void insert(LogEntry logEntry) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("appName", logEntry.appName);
        values.put("tag", logEntry.tag);
        values.put("message", logEntry.message);
        values.put("level", logEntry.level);
        values.put("timestamp", logEntry.timestamp);
        
        db.insert(TABLE_LOGS, null, values);
    }

    public List<LogEntry> getAllLogs() {
        return getLogsInternal("SELECT * FROM " + TABLE_LOGS + " ORDER BY timestamp ASC");
    }

    public List<LogEntry> getLogsByApp(String appName) {
        return getLogsInternal("SELECT * FROM " + TABLE_LOGS + " WHERE appName = '" + appName + "' ORDER BY timestamp ASC");
    }

    public List<LogEntry> getLogsByLevel(String level) {
        return getLogsInternal("SELECT * FROM " + TABLE_LOGS + " WHERE level = '" + level + "' ORDER BY timestamp ASC");
    }
    
    public List<LogEntry> getCrashLogs() {
        return getLogsInternal("SELECT * FROM " + TABLE_LOGS + " WHERE tag = 'CRASH' ORDER BY timestamp ASC");
    }

    private List<LogEntry> getLogsInternal(String query) {
        List<LogEntry> logs = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                LogEntry log = new LogEntry();
                log.id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                log.appName = cursor.getString(cursor.getColumnIndexOrThrow("appName"));
                log.tag = cursor.getString(cursor.getColumnIndexOrThrow("tag"));
                log.message = cursor.getString(cursor.getColumnIndexOrThrow("message"));
                log.level = cursor.getString(cursor.getColumnIndexOrThrow("level"));
                log.timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp"));
                logs.add(log);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return logs;
    }

    public void deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_LOGS);
    }

    public List<String> getAllAppNames() {
        List<String> names = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT appName FROM " + TABLE_LOGS + " ORDER BY appName", null);
        
        if (cursor.moveToFirst()) {
            do {
                names.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return names;
    }
}

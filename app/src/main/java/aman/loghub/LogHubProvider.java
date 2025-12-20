package aman.loghub;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class LogHubProvider extends ContentProvider {
    
    public static final String AUTHORITY = "aman.loghub.provider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/logs");
    public static final String ACTION_NEW_LOG = "aman.loghub.NEW_LOG";
    
    @Override
    public boolean onCreate() {
        return true;
    }
    
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        if (values == null) return null;
        
        try {
            String appName = values.getAsString("app_name");
            String tag = values.getAsString("tag");
            String message = values.getAsString("message");
            String level = values.getAsString("level");
            Long timestamp = values.getAsLong("timestamp");
            
            if (timestamp == null) timestamp = System.currentTimeMillis();
            
            LogEntry entry = new LogEntry(appName, tag, message, level, timestamp);
            
            new Thread(() -> {
                LogDatabase.getInstance(getContext()).logDao().insert(entry);
                
                Intent intent = new Intent(ACTION_NEW_LOG);
                intent.putExtra("log_entry", entry);
                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
            }).start();
            
            return Uri.withAppendedPath(CONTENT_URI, String.valueOf(timestamp));
            
        } catch (Exception e) {
            return null;
        }
    }
    
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, 
                       @Nullable String selection, @Nullable String[] selectionArgs, 
                       @Nullable String sortOrder) {
        return null;
    }
    
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return "vnd.android.cursor.item/vnd.aman.loghub.log";
    }
    
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, 
                     @Nullable String[] selectionArgs) {
        return 0;
    }
    
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, 
                     @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
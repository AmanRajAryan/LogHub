package aman.loghub;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class LogDao_Impl implements LogDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<LogEntry> __insertionAdapterOfLogEntry;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public LogDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfLogEntry = new EntityInsertionAdapter<LogEntry>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `logs` (`id`,`appName`,`tag`,`message`,`level`,`timestamp`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final LogEntry entity) {
        statement.bindLong(1, entity.id);
        if (entity.appName == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.appName);
        }
        if (entity.tag == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.tag);
        }
        if (entity.message == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.message);
        }
        if (entity.level == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.level);
        }
        statement.bindLong(6, entity.timestamp);
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM logs";
        return _query;
      }
    };
  }

  @Override
  public void insert(final LogEntry logEntry) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfLogEntry.insert(logEntry);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteAll() {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAll.acquire();
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteAll.release(_stmt);
    }
  }

  @Override
  public List<LogEntry> getAllLogs() {
    final String _sql = "SELECT * FROM logs ORDER BY timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfAppName = CursorUtil.getColumnIndexOrThrow(_cursor, "appName");
      final int _cursorIndexOfTag = CursorUtil.getColumnIndexOrThrow(_cursor, "tag");
      final int _cursorIndexOfMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "message");
      final int _cursorIndexOfLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "level");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final List<LogEntry> _result = new ArrayList<LogEntry>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final LogEntry _item;
        final String _tmpAppName;
        if (_cursor.isNull(_cursorIndexOfAppName)) {
          _tmpAppName = null;
        } else {
          _tmpAppName = _cursor.getString(_cursorIndexOfAppName);
        }
        final String _tmpTag;
        if (_cursor.isNull(_cursorIndexOfTag)) {
          _tmpTag = null;
        } else {
          _tmpTag = _cursor.getString(_cursorIndexOfTag);
        }
        final String _tmpMessage;
        if (_cursor.isNull(_cursorIndexOfMessage)) {
          _tmpMessage = null;
        } else {
          _tmpMessage = _cursor.getString(_cursorIndexOfMessage);
        }
        final String _tmpLevel;
        if (_cursor.isNull(_cursorIndexOfLevel)) {
          _tmpLevel = null;
        } else {
          _tmpLevel = _cursor.getString(_cursorIndexOfLevel);
        }
        final long _tmpTimestamp;
        _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
        _item = new LogEntry(_tmpAppName,_tmpTag,_tmpMessage,_tmpLevel,_tmpTimestamp);
        _item.id = _cursor.getInt(_cursorIndexOfId);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<LogEntry> getLogsByApp(final String appName) {
    final String _sql = "SELECT * FROM logs WHERE appName = ? ORDER BY timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (appName == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, appName);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfAppName = CursorUtil.getColumnIndexOrThrow(_cursor, "appName");
      final int _cursorIndexOfTag = CursorUtil.getColumnIndexOrThrow(_cursor, "tag");
      final int _cursorIndexOfMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "message");
      final int _cursorIndexOfLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "level");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final List<LogEntry> _result = new ArrayList<LogEntry>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final LogEntry _item;
        final String _tmpAppName;
        if (_cursor.isNull(_cursorIndexOfAppName)) {
          _tmpAppName = null;
        } else {
          _tmpAppName = _cursor.getString(_cursorIndexOfAppName);
        }
        final String _tmpTag;
        if (_cursor.isNull(_cursorIndexOfTag)) {
          _tmpTag = null;
        } else {
          _tmpTag = _cursor.getString(_cursorIndexOfTag);
        }
        final String _tmpMessage;
        if (_cursor.isNull(_cursorIndexOfMessage)) {
          _tmpMessage = null;
        } else {
          _tmpMessage = _cursor.getString(_cursorIndexOfMessage);
        }
        final String _tmpLevel;
        if (_cursor.isNull(_cursorIndexOfLevel)) {
          _tmpLevel = null;
        } else {
          _tmpLevel = _cursor.getString(_cursorIndexOfLevel);
        }
        final long _tmpTimestamp;
        _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
        _item = new LogEntry(_tmpAppName,_tmpTag,_tmpMessage,_tmpLevel,_tmpTimestamp);
        _item.id = _cursor.getInt(_cursorIndexOfId);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<LogEntry> getLogsByLevel(final String level) {
    final String _sql = "SELECT * FROM logs WHERE level = ? ORDER BY timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (level == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, level);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfAppName = CursorUtil.getColumnIndexOrThrow(_cursor, "appName");
      final int _cursorIndexOfTag = CursorUtil.getColumnIndexOrThrow(_cursor, "tag");
      final int _cursorIndexOfMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "message");
      final int _cursorIndexOfLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "level");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final List<LogEntry> _result = new ArrayList<LogEntry>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final LogEntry _item;
        final String _tmpAppName;
        if (_cursor.isNull(_cursorIndexOfAppName)) {
          _tmpAppName = null;
        } else {
          _tmpAppName = _cursor.getString(_cursorIndexOfAppName);
        }
        final String _tmpTag;
        if (_cursor.isNull(_cursorIndexOfTag)) {
          _tmpTag = null;
        } else {
          _tmpTag = _cursor.getString(_cursorIndexOfTag);
        }
        final String _tmpMessage;
        if (_cursor.isNull(_cursorIndexOfMessage)) {
          _tmpMessage = null;
        } else {
          _tmpMessage = _cursor.getString(_cursorIndexOfMessage);
        }
        final String _tmpLevel;
        if (_cursor.isNull(_cursorIndexOfLevel)) {
          _tmpLevel = null;
        } else {
          _tmpLevel = _cursor.getString(_cursorIndexOfLevel);
        }
        final long _tmpTimestamp;
        _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
        _item = new LogEntry(_tmpAppName,_tmpTag,_tmpMessage,_tmpLevel,_tmpTimestamp);
        _item.id = _cursor.getInt(_cursorIndexOfId);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<LogEntry> getCrashLogs() {
    final String _sql = "SELECT * FROM logs WHERE tag = 'CRASH' ORDER BY timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfAppName = CursorUtil.getColumnIndexOrThrow(_cursor, "appName");
      final int _cursorIndexOfTag = CursorUtil.getColumnIndexOrThrow(_cursor, "tag");
      final int _cursorIndexOfMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "message");
      final int _cursorIndexOfLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "level");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final List<LogEntry> _result = new ArrayList<LogEntry>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final LogEntry _item;
        final String _tmpAppName;
        if (_cursor.isNull(_cursorIndexOfAppName)) {
          _tmpAppName = null;
        } else {
          _tmpAppName = _cursor.getString(_cursorIndexOfAppName);
        }
        final String _tmpTag;
        if (_cursor.isNull(_cursorIndexOfTag)) {
          _tmpTag = null;
        } else {
          _tmpTag = _cursor.getString(_cursorIndexOfTag);
        }
        final String _tmpMessage;
        if (_cursor.isNull(_cursorIndexOfMessage)) {
          _tmpMessage = null;
        } else {
          _tmpMessage = _cursor.getString(_cursorIndexOfMessage);
        }
        final String _tmpLevel;
        if (_cursor.isNull(_cursorIndexOfLevel)) {
          _tmpLevel = null;
        } else {
          _tmpLevel = _cursor.getString(_cursorIndexOfLevel);
        }
        final long _tmpTimestamp;
        _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
        _item = new LogEntry(_tmpAppName,_tmpTag,_tmpMessage,_tmpLevel,_tmpTimestamp);
        _item.id = _cursor.getInt(_cursorIndexOfId);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<String> getAllAppNames() {
    final String _sql = "SELECT DISTINCT appName FROM logs ORDER BY appName";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final List<String> _result = new ArrayList<String>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final String _item;
        if (_cursor.isNull(0)) {
          _item = null;
        } else {
          _item = _cursor.getString(0);
        }
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public int getLogCount() {
    final String _sql = "SELECT COUNT(*) FROM logs";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _result;
      if (_cursor.moveToFirst()) {
        _result = _cursor.getInt(0);
      } else {
        _result = 0;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public int getLogCountByLevel(final String level) {
    final String _sql = "SELECT COUNT(*) FROM logs WHERE level = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (level == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, level);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _result;
      if (_cursor.moveToFirst()) {
        _result = _cursor.getInt(0);
      } else {
        _result = 0;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public int getCrashCount() {
    final String _sql = "SELECT COUNT(*) FROM logs WHERE tag = 'CRASH'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _result;
      if (_cursor.moveToFirst()) {
        _result = _cursor.getInt(0);
      } else {
        _result = 0;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}

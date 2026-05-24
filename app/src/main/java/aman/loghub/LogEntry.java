package aman.loghub;

import android.os.Parcel;
import android.os.Parcelable;
import java.io.Serializable;

public class LogEntry implements Parcelable, Serializable {
    public int id;
    public String appName;
    public String tag;
    public String message;
    public String level;
    public long timestamp;

    public LogEntry() {}

    protected LogEntry(Parcel in) {
        id = in.readInt();
        appName = in.readString();
        tag = in.readString();
        message = in.readString();
        level = in.readString();
        timestamp = in.readLong();
    }

    public static final Creator<LogEntry> CREATOR = new Creator<LogEntry>() {
        @Override
        public LogEntry createFromParcel(Parcel in) {
            return new LogEntry(in);
        }

        @Override
        public LogEntry[] newArray(int size) {
            return new LogEntry[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(appName);
        dest.writeString(tag);
        dest.writeString(message);
        dest.writeString(level);
        dest.writeLong(timestamp);
    }
}

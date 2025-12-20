package aman.loghub;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter for displaying log entries with Material Design
 */
public class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {
    
    private List<LogEntry> logs;
    private List<LogEntry> logsFiltered;
    private Context context;
    private SimpleDateFormat timeFormat;
    private int expandedPosition = -1;
    
    public LogAdapter(Context context) {
        this.context = context;
        this.logs = new ArrayList<>();
        this.logsFiltered = new ArrayList<>();
        this.timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    }
    
    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_log, parent, false);
        return new LogViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        LogEntry log = logsFiltered.get(position);
        boolean isExpanded = position == expandedPosition;
        
        // Set level chip
        holder.levelChip.setText(log.level);
        holder.levelChip.setChipBackgroundColorResource(getLogLevelColor(log.level));
        holder.levelChip.setTextColor(context.getColor(getLogLevelTextColor(log.level)));
        
        // Set app name and tag
        holder.appNameTag.setText(log.appName + "/" + log.tag);
        
        // Set timestamp
        holder.timestamp.setText(timeFormat.format(new Date(log.timestamp)));
        
        // Set message (truncated)
        holder.message.setText(log.message);
        
        // Show expand indicator if message is long
        if (log.message.length() > 100) {
            holder.expandIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.expandIndicator.setVisibility(View.GONE);
        }
        
        // Show/hide expanded content
        if (isExpanded) {
            holder.message.setMaxLines(3);
            holder.fullMessage.setText(log.message);
            holder.fullMessage.setVisibility(View.VISIBLE);
            holder.actionButtonsLayout.setVisibility(View.VISIBLE);
            holder.expandIndicator.setVisibility(View.GONE);
            
            // Set card background color based on level
            holder.logCard.setCardBackgroundColor(
                context.getColor(getLogLevelBackgroundColor(log.level))
            );
        } else {
            holder.message.setMaxLines(3);
            holder.fullMessage.setVisibility(View.GONE);
            holder.actionButtonsLayout.setVisibility(View.GONE);
            holder.logCard.setCardBackgroundColor(
                context.getColor(R.color.card_background)
            );
        }
        
        // Click to expand/collapse
        holder.itemView.setOnClickListener(v -> {
            int previousExpandedPosition = expandedPosition;
            if (isExpanded) {
                expandedPosition = -1;
            } else {
                expandedPosition = holder.getAdapterPosition();
            }
            
            if (previousExpandedPosition != -1) {
                notifyItemChanged(previousExpandedPosition);
            }
            notifyItemChanged(holder.getAdapterPosition());
        });
        
        // Copy button
        holder.btnCopy.setOnClickListener(v -> {
            copyToClipboard(log);
        });
        
        // Share button
        holder.btnShare.setOnClickListener(v -> {
            shareLog(log);
        });
    }
    
    @Override
    public int getItemCount() {
        return logsFiltered.size();
    }
    
    /**
     * Update the full dataset
     */
    public void setLogs(List<LogEntry> logs) {
        this.logs = new ArrayList<>(logs);
        this.logsFiltered = new ArrayList<>(logs);
        expandedPosition = -1;
        notifyDataSetChanged();
    }
    
    /**
     * Add a single log entry (for real-time updates)
     */
    public void addLog(LogEntry log) {
        logs.add(log);
        logsFiltered.add(log);
        notifyItemInserted(logsFiltered.size() - 1);
    }
    
    /**
     * Filter logs by level
     */
    public void filterByLevel(String level) {
        if (level == null || level.equals("ALL")) {
            logsFiltered = new ArrayList<>(logs);
        } else {
            logsFiltered = new ArrayList<>();
            for (LogEntry log : logs) {
                if (log.level.equals(level) || 
                    (level.equals("CRASH") && log.tag.equals("CRASH"))) {
                    logsFiltered.add(log);
                }
            }
        }
        expandedPosition = -1;
        notifyDataSetChanged();
    }
    
    /**
     * Filter logs by app name
     */
    public void filterByApp(String appName) {
        if (appName == null || appName.isEmpty()) {
            logsFiltered = new ArrayList<>(logs);
        } else {
            logsFiltered = new ArrayList<>();
            for (LogEntry log : logs) {
                if (log.appName.equals(appName)) {
                    logsFiltered.add(log);
                }
            }
        }
        expandedPosition = -1;
        notifyDataSetChanged();
    }
    
    /**
     * Search logs by keyword
     */
    public void search(String query) {
        if (query == null || query.trim().isEmpty()) {
            logsFiltered = new ArrayList<>(logs);
        } else {
            logsFiltered = new ArrayList<>();
            String lowerQuery = query.toLowerCase();
            for (LogEntry log : logs) {
                if (log.message.toLowerCase().contains(lowerQuery) ||
                    log.tag.toLowerCase().contains(lowerQuery) ||
                    log.appName.toLowerCase().contains(lowerQuery)) {
                    logsFiltered.add(log);
                }
            }
        }
        expandedPosition = -1;
        notifyDataSetChanged();
    }
    
    /**
     * Clear all logs
     */
    public void clear() {
        logs.clear();
        logsFiltered.clear();
        expandedPosition = -1;
        notifyDataSetChanged();
    }
    
    /**
     * Helper to get the filtered list for Text View mode
     */
    public List<LogEntry> getFilteredLogs() {
        return logsFiltered;
    }
    
    /**
     * Get log level background color
     */
    private int getLogLevelBackgroundColor(String level) {
        switch (level) {
            case "ERROR":
                return R.color.log_error_bg;
            case "WARN":
                return R.color.log_warn_bg;
            case "INFO":
                return R.color.log_info_bg;
            case "DEBUG":
                return R.color.log_debug_bg;
            default:
                if (level.equals("CRASH")) {
                    return R.color.log_crash_bg;
                }
                return R.color.card_background;
        }
    }
    
    /**
     * Get log level chip color
     */
    private int getLogLevelColor(String level) {
        switch (level) {
            case "ERROR":
                return R.color.log_error_bg;
            case "WARN":
                return R.color.log_warn_bg;
            case "INFO":
                return R.color.log_info_bg;
            case "DEBUG":
                return R.color.log_debug_bg;
            default:
                return R.color.log_crash_bg;
        }
    }
    
    /**
     * Get log level text color
     */
    private int getLogLevelTextColor(String level) {
        switch (level) {
            case "ERROR":
                return R.color.log_error;
            case "WARN":
                return R.color.log_warn;
            case "INFO":
                return R.color.log_info;
            case "DEBUG":
                return R.color.log_debug;
            default:
                return R.color.log_crash;
        }
    }
    
    /**
     * Copy log to clipboard
     */
    private void copyToClipboard(LogEntry log) {
        ClipboardManager clipboard = (ClipboardManager) 
            context.getSystemService(Context.CLIPBOARD_SERVICE);
        
        String text = String.format("[%s] %s/%s\n%s\n%s",
            log.level,
            log.appName,
            log.tag,
            timeFormat.format(new Date(log.timestamp)),
            log.message
        );
        
        ClipData clip = ClipData.newPlainText("Log Entry", text);
        clipboard.setPrimaryClip(clip);
        
        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Share log via Android share sheet
     */
    private void shareLog(LogEntry log) {
        String text = String.format("[%s] %s/%s\n%s\n\n%s",
            log.level,
            log.appName,
            log.tag,
            timeFormat.format(new Date(log.timestamp)),
            log.message
        );
        
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Log Entry from " + log.appName);
        
        context.startActivity(Intent.createChooser(shareIntent, "Share log via"));
    }
    
    /**
     * ViewHolder for log items
     */
    static class LogViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView logCard;
        Chip levelChip;
        TextView appNameTag;
        TextView timestamp;
        TextView message;
        TextView expandIndicator;
        TextView fullMessage;
        View actionButtonsLayout;
        MaterialButton btnCopy;
        MaterialButton btnShare;
        
        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            logCard = itemView.findViewById(R.id.logCard);
            levelChip = itemView.findViewById(R.id.levelChip);
            appNameTag = itemView.findViewById(R.id.appNameTag);
            timestamp = itemView.findViewById(R.id.timestamp);
            message = itemView.findViewById(R.id.message);
            expandIndicator = itemView.findViewById(R.id.expandIndicator);
            fullMessage = itemView.findViewById(R.id.fullMessage);
            actionButtonsLayout = itemView.findViewById(R.id.actionButtonsLayout);
            btnCopy = itemView.findViewById(R.id.btnCopy);
            btnShare = itemView.findViewById(R.id.btnShare);
        }
    }
}

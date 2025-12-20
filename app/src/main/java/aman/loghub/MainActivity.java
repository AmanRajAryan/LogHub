package aman.loghub;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Main activity for LogHub - Modern Material Design UI */
public class MainActivity extends AppCompatActivity {

    // UI Components
    private MaterialToolbar toolbar;
    private TextInputLayout searchLayout;
    private TextInputEditText searchEditText;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private ExtendedFloatingActionButton fabClear;
    private View emptyStateLayout;
    private ChipGroup filterChipGroup;

    // Text Viewer Components
    private ScrollView textLogContainer;
    private TextView logTextView;
    private boolean isTextViewMode = false;
    private MenuItem toggleViewMenuItem;

    // Search state
    private boolean isSearchVisible = false;
    private MenuItem searchMenuItem;

    // Handler for debouncing text view refresh
    private final android.os.Handler searchHandler =
            new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable searchRunnable;

    // Data
    private LogAdapter adapter;
    private List<LogEntry> allLogs;
    private BroadcastReceiver receiver;

    // SAF Launcher for Exporting
    private ActivityResultLauncher<String> exportLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize SAF Launcher (Must be done in onCreate)
        setupExportLauncher();

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSearch();
        setupFilters();
        setupFAB();
        setupBroadcastReceiver();
        loadLogs();
    }

    /** Initialize SAF Launcher */
    private void setupExportLauncher() {
        exportLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.CreateDocument("text/plain"),
                        uri -> {
                            if (uri != null) {
                                saveLogsToUri(uri);
                            }
                        });
    }

    /** Initialize all views */
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        searchLayout = findViewById(R.id.searchLayout);
        searchEditText = findViewById(R.id.searchEditText);
        recyclerView = findViewById(R.id.logsRecyclerView);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        fabClear = findViewById(R.id.fabClear);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        filterChipGroup = findViewById(R.id.filterChipGroup);

        // New Text Viewer Components
        textLogContainer = findViewById(R.id.textLogContainer);
        logTextView = findViewById(R.id.logTextView);
    }

    /** Setup toolbar with menu */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
    }

    /** Setup RecyclerView with adapter */
    private void setupRecyclerView() {
        adapter = new LogAdapter(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(false);
        recyclerView.setNestedScrollingEnabled(false);

        // Swipe to refresh
        swipeRefresh.setOnRefreshListener(
                () -> {
                    loadLogs();
                    swipeRefresh.setRefreshing(false);
                });
    }

    /** Setup search functionality */
    private void setupSearch() {
        searchEditText.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        adapter.search(s.toString());
                        updateEmptyState();

                        // For text view mode: debounce the refresh to prevent focus loss
                        if (isTextViewMode) {
                            // Cancel any pending refresh
                            if (searchRunnable != null) {
                                searchHandler.removeCallbacks(searchRunnable);
                            }

                            // Schedule new refresh with delay
                            searchRunnable = () -> refreshTextViewSafe();
                            searchHandler.postDelayed(searchRunnable, 200); // 200ms delay
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {}
                });
    }

    /** Toggle search bar visibility */
    private void toggleSearch() {
        isSearchVisible = !isSearchVisible;

        if (isSearchVisible) {
            searchLayout.setVisibility(View.VISIBLE);
            searchEditText.requestFocus();
            // Show keyboard
            searchEditText.postDelayed(
                    () -> {
                        android.view.inputmethod.InputMethodManager imm =
                                (android.view.inputmethod.InputMethodManager)
                                        getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(
                                searchEditText,
                                android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                    },
                    100);

            // Update menu icon to close
            if (searchMenuItem != null) {
                searchMenuItem.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
            }
        } else {
            searchLayout.setVisibility(View.GONE);
            searchEditText.setText("");
            // Hide keyboard
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);

            // Update menu icon to search
            if (searchMenuItem != null) {
                searchMenuItem.setIcon(android.R.drawable.ic_menu_search);
            }
        }
    }

    /** Setup filter chips */
    private void setupFilters() {
        filterChipGroup.setOnCheckedStateChangeListener(
                (group, checkedIds) -> {
                    if (checkedIds.isEmpty()) {
                        adapter.filterByLevel(null);
                    } else {
                        int checkedId = checkedIds.get(0);

                        if (checkedId == R.id.chipAll) {
                            adapter.filterByLevel(null);
                        } else if (checkedId == R.id.chipDebug) {
                            adapter.filterByLevel("DEBUG");
                        } else if (checkedId == R.id.chipInfo) {
                            adapter.filterByLevel("INFO");
                        } else if (checkedId == R.id.chipWarn) {
                            adapter.filterByLevel("WARN");
                        } else if (checkedId == R.id.chipError) {
                            adapter.filterByLevel("ERROR");
                        } else if (checkedId == R.id.chipCrash) {
                            adapter.filterByLevel("CRASH");
                        }
                    }
                    updateStatistics();
                    if (isTextViewMode) refreshTextView();
                });
    }

    /** Setup Floating Action Button */
    private void setupFAB() {
        fabClear.setOnClickListener(
                v -> {
                    showClearConfirmationDialog();
                });
    }

    /** Setup broadcast receiver for real-time log updates */
    private void setupBroadcastReceiver() {
        receiver =
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        LogEntry entry = (LogEntry) intent.getSerializableExtra("log_entry");
                        if (entry != null) {
                            adapter.addLog(entry);

                            if (!isTextViewMode) {
                                recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
                            } else {
                                // In text mode, refresh to show new log
                                refreshTextView();
                            }

                            updateStatistics();
                            updateEmptyState();
                        }
                    }
                };

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(receiver, new IntentFilter(LogHubProvider.ACTION_NEW_LOG));
    }

    /** Load logs from database */
    private void loadLogs() {
        new Thread(
                        () -> {
                            allLogs = LogDatabase.getInstance(this).logDao().getAllLogs();
                            runOnUiThread(
                                    () -> {
                                        adapter.setLogs(allLogs);
                                        updateStatistics();
                                        updateEmptyState();
                                        // Scroll to bottom after loading
                                        if (adapter.getItemCount() > 0 && !isTextViewMode) {
                                            recyclerView.scrollToPosition(
                                                    adapter.getItemCount() - 1);
                                        }
                                        if (isTextViewMode) refreshTextView();
                                    });
                        })
                .start();
    }

    /** Update statistics cards */
    private void updateStatistics() {
        new Thread(
                        () -> {
                            List<LogEntry> logs =
                                    LogDatabase.getInstance(this).logDao().getAllLogs();

                            int total = logs.size();
                            int errors = 0;
                            int warnings = 0;
                            int crashes = 0;

                            for (LogEntry log : logs) {
                                if (log.level.equals("ERROR")) {
                                    errors++;
                                }
                                if (log.level.equals("WARN")) {
                                    warnings++;
                                }
                                if (log.tag.equals("CRASH")) {
                                    crashes++;
                                }
                            }
                            // UI update logic for stats would go here
                        })
                .start();
    }

    /** * Update visibility of Empty State, Recycler View, and Text View */
    private void updateEmptyState() {
        if (adapter.getItemCount() == 0) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            swipeRefresh.setVisibility(View.GONE); // Hide entire list container
            textLogContainer.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            if (isTextViewMode) {
                textLogContainer.setVisibility(View.VISIBLE);
                swipeRefresh.setVisibility(View.GONE);
            } else {
                swipeRefresh.setVisibility(View.VISIBLE); // Show entire list container
                textLogContainer.setVisibility(View.GONE);

                // Explicitly ensure inner recycler is visible
                recyclerView.setVisibility(View.VISIBLE);
            }
        }
    }

    /** Show clear confirmation dialog */
    private void showClearConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Clear All Logs?")
                .setMessage("This will permanently delete all logs. This action cannot be undone.")
                .setPositiveButton(
                        "Clear",
                        (dialog, which) -> {
                            clearAllLogs();
                        })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /** Clear all logs from database */
    private void clearAllLogs() {
        new Thread(
                        () -> {
                            LogDatabase.getInstance(this).logDao().deleteAll();
                            runOnUiThread(
                                    () -> {
                                        adapter.clear();
                                        logTextView.setText("");
                                        updateStatistics();
                                        updateEmptyState();
                                        Toast.makeText(this, "All logs cleared", Toast.LENGTH_SHORT)
                                                .show();
                                    });
                        })
                .start();
    }

    /** Show filter by app dialog */
    private void showFilterByAppDialog() {
        new Thread(
                        () -> {
                            List<String> appNames =
                                    LogDatabase.getInstance(this).logDao().getAllAppNames();

                            runOnUiThread(
                                    () -> {
                                        if (appNames.isEmpty()) {
                                            Toast.makeText(
                                                            this,
                                                            "No apps found",
                                                            Toast.LENGTH_SHORT)
                                                    .show();
                                            return;
                                        }

                                        String[] appArray = appNames.toArray(new String[0]);

                                        new AlertDialog.Builder(this)
                                                .setTitle("Filter by App")
                                                .setItems(
                                                        appArray,
                                                        (dialog, which) -> {
                                                            String selectedApp = appArray[which];
                                                            adapter.filterByApp(selectedApp);
                                                            updateEmptyState();
                                                            if (isTextViewMode) refreshTextView();
                                                            Toast.makeText(
                                                                            this,
                                                                            "Showing logs from: "
                                                                                    + selectedApp,
                                                                            Toast.LENGTH_SHORT)
                                                                    .show();
                                                        })
                                                .setNegativeButton(
                                                        "Show All",
                                                        (dialog, which) -> {
                                                            adapter.filterByApp(null);
                                                            updateEmptyState();
                                                            if (isTextViewMode) refreshTextView();
                                                        })
                                                .show();
                                    });
                        })
                .start();
    }

    /** * Trigger SAF Directory/File Picker */
    private void initiateExport() {
        String fileName =
                "loghub_export_"
                        + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                                .format(new Date())
                        + ".txt";
        exportLauncher.launch(fileName);
    }

    /** * Write logs to the URI selected by the user via SAF */
    private void saveLogsToUri(Uri uri) {
        new Thread(
                        () -> {
                            try {
                                // Fetch logs
                                List<LogEntry> logs =
                                        LogDatabase.getInstance(this).logDao().getAllLogs();
                                if (logs.isEmpty()) {
                                    runOnUiThread(
                                            () ->
                                                    Toast.makeText(
                                                                    this,
                                                                    "No logs to export",
                                                                    Toast.LENGTH_SHORT)
                                                            .show());
                                    return;
                                }

                                // Open OutputStream via ContentResolver
                                try (OutputStream out = getContentResolver().openOutputStream(uri);
                                        PrintWriter writer = new PrintWriter(out)) {

                                    SimpleDateFormat dateFormat =
                                            new SimpleDateFormat(
                                                    "yyyy-MM-dd HH:mm:ss", Locale.getDefault());

                                    writer.write("LogHub Export\n");
                                    writer.write(
                                            "Generated: " + dateFormat.format(new Date()) + "\n");
                                    writer.write("Total Logs: " + logs.size() + "\n");
                                    writer.write(
                                            "================================================================================\n\n");

                                    for (LogEntry log : logs) {
                                        writer.write(
                                                String.format(
                                                        "[%s] %s/%s\n",
                                                        log.level, log.appName, log.tag));
                                        writer.write(
                                                "Time: "
                                                        + dateFormat.format(new Date(log.timestamp))
                                                        + "\n");
                                        writer.write("Message: " + log.message + "\n");
                                        writer.write(
                                                "--------------------------------------------------------------------------------\n\n");
                                    }
                                }

                                runOnUiThread(
                                        () ->
                                                Toast.makeText(
                                                                this,
                                                                "Logs saved successfully",
                                                                Toast.LENGTH_LONG)
                                                        .show());

                            } catch (Exception e) {
                                runOnUiThread(
                                        () ->
                                                Toast.makeText(
                                                                this,
                                                                "Export failed: " + e.getMessage(),
                                                                Toast.LENGTH_LONG)
                                                        .show());
                            }
                        })
                .start();
    }

    /** * Toggle between List View and Text View. */
    private void toggleViewMode() {
        isTextViewMode = !isTextViewMode;

        // Delegate visibility handling to the central method
        updateEmptyState();

        if (isTextViewMode) {
            // Switched to Text Mode
            toggleViewMenuItem.setIcon(R.drawable.ic_view_agenda);
            refreshTextView();
        } else {
            // Switched to List Mode
            toggleViewMenuItem.setIcon(R.drawable.ic_subject);

            // Scroll to bottom if content exists
            if (adapter.getItemCount() > 0) {
                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
            }
        }
    }

    /**
     * Generate Logcat-style syntax highlighting (Darcula Theme). Safe version that preserves search
     * bar focus.
     */
    private void refreshTextViewSafe() {
        if (!isTextViewMode) return;

        // Store focus state BEFORE starting background work
        final boolean hadFocus = searchEditText != null && searchEditText.hasFocus();
        final int cursorPosition = hadFocus ? searchEditText.getSelectionStart() : -1;

        // Create a snapshot copy to avoid ConcurrentModificationException
        final List<LogEntry> allFilteredLogs = new java.util.ArrayList<>(adapter.getFilteredLogs());

        new Thread(
                        () -> {
                            if (allFilteredLogs.isEmpty()) {
                                runOnUiThread(
                                        () -> {
                                            logTextView.setText("No logs to display");
                                        });
                                return;
                            }

                            // --- PERFORMANCE LIMIT ---
                            int MAX_LOGS = 1000;
                            int totalLogs = allFilteredLogs.size();
                            int startIndex = Math.max(0, totalLogs - MAX_LOGS);

                            List<LogEntry> logsToShow =
                                    allFilteredLogs.subList(startIndex, totalLogs);

                            SpannableStringBuilder builder = new SpannableStringBuilder();

                            if (startIndex > 0) {
                                String warning =
                                        String.format(
                                                Locale.US,
                                                "--- Displaying last %d of %d logs (Truncated for performance) ---\n\n",
                                                MAX_LOGS,
                                                totalLogs);
                                builder.append(warning);
                                builder.setSpan(
                                        new ForegroundColorSpan(0xFF808080),
                                        0,
                                        warning.length(),
                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                builder.setSpan(
                                        new StyleSpan(Typeface.ITALIC),
                                        0,
                                        warning.length(),
                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }

                            SimpleDateFormat timeFormat =
                                    new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault());

                            // --- Darcula Palette ---
                            int colorTime = 0xFF808080;
                            int colorText = 0xFFA9B7C6;
                            int colorLink = 0xFF287BDE;
                            int colorDebug = 0xFF6897BB;
                            int colorInfo = 0xFF6A8759;
                            int colorWarn = 0xFFBBB529;
                            int colorError = 0xFFFF6B68;
                            int colorAssert = 0xFF9876AA;

                            Pattern stackTracePattern = Pattern.compile("at\\s+(.*?)\\((.*?)\\)");

                            for (LogEntry log : logsToShow) {
                                int lineStart = builder.length();
                                builder.append(timeFormat.format(new Date(log.timestamp)))
                                        .append("  ");
                                builder.setSpan(
                                        new ForegroundColorSpan(colorTime),
                                        lineStart,
                                        builder.length(),
                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                int levelColor = colorText;
                                String levelLabel = "V";

                                switch (log.level) {
                                    case "DEBUG":
                                        levelColor = colorDebug;
                                        levelLabel = "D";
                                        break;
                                    case "INFO":
                                        levelColor = colorInfo;
                                        levelLabel = "I";
                                        break;
                                    case "WARN":
                                        levelColor = colorWarn;
                                        levelLabel = "W";
                                        break;
                                    case "ERROR":
                                        levelColor = colorError;
                                        levelLabel = "E";
                                        break;
                                    case "CRASH":
                                        levelColor = colorError;
                                        levelLabel = "E";
                                        break;
                                    case "ASSERT":
                                        levelColor = colorAssert;
                                        levelLabel = "A";
                                        break;
                                }

                                int tagStart = builder.length();
                                builder.append(levelLabel).append("/").append(log.tag).append(": ");
                                builder.setSpan(
                                        new ForegroundColorSpan(levelColor),
                                        tagStart,
                                        builder.length(),
                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                int msgStart = builder.length();
                                String message = log.message;
                                builder.append(message);

                                int msgColor =
                                        (log.level.equals("ERROR") || log.level.equals("CRASH"))
                                                ? colorError
                                                : colorText;
                                builder.setSpan(
                                        new ForegroundColorSpan(msgColor),
                                        msgStart,
                                        builder.length(),
                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                if (message.contains("at ") && message.contains("(")) {
                                    Matcher matcher = stackTracePattern.matcher(message);
                                    while (matcher.find()) {
                                        int start = matcher.start(2);
                                        int end = matcher.end(2);
                                        if (start >= 0 && end <= message.length()) {
                                            builder.setSpan(
                                                    new ForegroundColorSpan(colorLink),
                                                    msgStart + start,
                                                    msgStart + end,
                                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        }
                                    }
                                }

                                builder.append("\n");
                            }

                            runOnUiThread(
                                    () -> {
                                        // Update text
                                        logTextView.setText(builder);

                                        // Restore focus if it was in search bar
                                        if (hadFocus && searchEditText != null) {
                                            searchEditText.post(
                                                    () -> {
                                                        searchEditText.requestFocus();
                                                        if (cursorPosition >= 0
                                                                && cursorPosition
                                                                        <= searchEditText
                                                                                .getText()
                                                                                .length()) {
                                                            searchEditText.setSelection(
                                                                    cursorPosition);
                                                        }
                                                    });
                                        } else if (!hadFocus) {
                                            // Only scroll if search doesn't have focus
                                            textLogContainer.post(
                                                    () ->
                                                            textLogContainer.fullScroll(
                                                                    View.FOCUS_DOWN));
                                        }
                                    });
                        })
                .start();
    }

    /** Original refresh method for non-search scenarios */
    private void refreshTextView() {
        refreshTextViewSafe();
    }

    /**
     * Generate Logcat-style syntax highlighting (Darcula Theme). Includes logic to PREVENT FOCUS
     * LOSS in Search Bar.
     */
    private void refreshTextViewOld() {
        if (!isTextViewMode) return;

        new Thread(
                        () -> {
                            List<LogEntry> allFilteredLogs = adapter.getFilteredLogs();

                            if (allFilteredLogs.isEmpty()) {
                                runOnUiThread(() -> logTextView.setText("No logs to display"));
                                return;
                            }

                            // --- PERFORMANCE LIMIT ---
                            int MAX_LOGS = 1000;
                            int totalLogs = allFilteredLogs.size();
                            int startIndex = Math.max(0, totalLogs - MAX_LOGS);

                            List<LogEntry> logsToShow =
                                    allFilteredLogs.subList(startIndex, totalLogs);

                            SpannableStringBuilder builder = new SpannableStringBuilder();

                            if (startIndex > 0) {
                                String warning =
                                        String.format(
                                                Locale.US,
                                                "--- Displaying last %d of %d logs (Truncated for performance) ---\n\n",
                                                MAX_LOGS,
                                                totalLogs);
                                builder.append(warning);
                                builder.setSpan(
                                        new ForegroundColorSpan(0xFF808080),
                                        0,
                                        warning.length(),
                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                builder.setSpan(
                                        new StyleSpan(Typeface.ITALIC),
                                        0,
                                        warning.length(),
                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }

                            SimpleDateFormat timeFormat =
                                    new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault());

                            // --- Darcula Palette ---
                            int colorTime = 0xFF808080;
                            int colorText = 0xFFA9B7C6;
                            int colorLink = 0xFF287BDE;
                            int colorDebug = 0xFF6897BB;
                            int colorInfo = 0xFF6A8759;
                            int colorWarn = 0xFFBBB529;
                            int colorError = 0xFFFF6B68;
                            int colorAssert = 0xFF9876AA;

                            Pattern stackTracePattern = Pattern.compile("at\\s+(.*?)\\((.*?)\\)");

                            for (LogEntry log : logsToShow) {
                                int lineStart = builder.length();
                                builder.append(timeFormat.format(new Date(log.timestamp)))
                                        .append("  ");
                                builder.setSpan(
                                        new ForegroundColorSpan(colorTime),
                                        lineStart,
                                        builder.length(),
                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                int levelColor = colorText;
                                String levelLabel = "V";

                                switch (log.level) {
                                    case "DEBUG":
                                        levelColor = colorDebug;
                                        levelLabel = "D";
                                        break;
                                    case "INFO":
                                        levelColor = colorInfo;
                                        levelLabel = "I";
                                        break;
                                    case "WARN":
                                        levelColor = colorWarn;
                                        levelLabel = "W";
                                        break;
                                    case "ERROR":
                                        levelColor = colorError;
                                        levelLabel = "E";
                                        break;
                                    case "CRASH":
                                        levelColor = colorError;
                                        levelLabel = "E";
                                        break;
                                    case "ASSERT":
                                        levelColor = colorAssert;
                                        levelLabel = "A";
                                        break;
                                }

                                int tagStart = builder.length();
                                builder.append(levelLabel).append("/").append(log.tag).append(": ");
                                builder.setSpan(
                                        new ForegroundColorSpan(levelColor),
                                        tagStart,
                                        builder.length(),
                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                int msgStart = builder.length();
                                String message = log.message;
                                builder.append(message);

                                int msgColor =
                                        (log.level.equals("ERROR") || log.level.equals("CRASH"))
                                                ? colorError
                                                : colorText;
                                builder.setSpan(
                                        new ForegroundColorSpan(msgColor),
                                        msgStart,
                                        builder.length(),
                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                if (message.contains("at ") && message.contains("(")) {
                                    Matcher matcher = stackTracePattern.matcher(message);
                                    while (matcher.find()) {
                                        int start = matcher.start(2);
                                        int end = matcher.end(2);
                                        if (start >= 0 && end <= message.length()) {
                                            builder.setSpan(
                                                    new ForegroundColorSpan(colorLink),
                                                    msgStart + start,
                                                    msgStart + end,
                                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        }
                                    }
                                }

                                builder.append("\n");
                            }

                            runOnUiThread(
                                    () -> {
                                        // Simple and clean - just update the text
                                        logTextView.setText(builder);

                                        // Only manage scrolling - don't interfere with focus
                                        if (searchEditText == null || !searchEditText.hasFocus()) {
                                            textLogContainer.post(
                                                    () ->
                                                            textLogContainer.fullScroll(
                                                                    View.FOCUS_DOWN));
                                        }
                                    });
                        })
                .start();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        searchMenuItem = menu.findItem(R.id.action_search);

        // Add Toggle View Item programmatically
        toggleViewMenuItem = menu.add(Menu.NONE, 1001, 1, "Toggle View");
        toggleViewMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        toggleViewMenuItem.setIcon(R.drawable.ic_subject); // Default

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == 1001) {
            toggleViewMode();
            return true;
        } else if (id == R.id.action_search) {
            toggleSearch();
            return true;
        } else if (id == R.id.action_refresh) {
            loadLogs();
            Toast.makeText(this, "Refreshed", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_export) {
            initiateExport();
            return true;
        } else if (id == R.id.action_filter_app) {
            showFilterByAppDialog();
            return true;
        } else if (id == R.id.action_settings) {
            Toast.makeText(this, "Settings coming soon", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (isSearchVisible) {
            toggleSearch();
        } else if (isTextViewMode) {
            toggleViewMode();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLogs();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);

        // Clean up search handler
        if (searchHandler != null) {
            searchHandler.removeCallbacksAndMessages(null);
        }
    }
}
